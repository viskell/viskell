package nl.utwente.group10.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.group10.ghcj.GhciSession;
import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.env.HaskellCatalog;
import nl.utwente.group10.haskell.expr.Expression;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.function.FunctionBlock;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.components.menu.GlobalMenu;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;
import nl.utwente.group10.ui.components.menu.FunctionMenu;

/**
 * Extension of TactilePane that keeps state for the user interface.
 */
public class CustomUIPane extends TactilePane {
    private ObjectProperty<Optional<Block>> selectedBlock;
    private ConnectionCreationManager connectionCreationManager;
    
    /**
     * Property that keeps track of Haskell errors occurring somewhere in the
     * program. This gets sets to true when an error first occurs somewhere,
     * and only gets set to false again when the entire program is error free.
     */
    private BooleanProperty errorOccurred;
    
    private Optional<GhciSession> ghci;
    private InspectorWindow inspector;

    private Point2D dragStart;
    private Point2D offset;
    
    /** Boolean to indicate that a drag (pan) action has started, yet not finished. */
    private boolean dragging;

    private HaskellCatalog catalog;
    private Environment envInstance;

    /** The File we're currently working on, if any. */
    private Optional<File> currentFile;

    /**
     * Maps expressions to function blocks for looking up the function block responsible for an expression in case of an
     * error.
     */
    private Map<Expression, FunctionBlock> exprToFunction;

    /**
     * Constructs a new instance.
     */
    public CustomUIPane(HaskellCatalog catalog) {
        this.connectionCreationManager = new ConnectionCreationManager(this);
        this.selectedBlock = new SimpleObjectProperty<>(Optional.empty());
        this.errorOccurred = new SimpleBooleanProperty(false);
        this.dragStart = Point2D.ZERO;
        this.offset = Point2D.ZERO;
        this.catalog = catalog;
        this.envInstance = catalog.asEnvironment();
        this.exprToFunction = new HashMap<Expression, FunctionBlock>();

        try {
            this.ghci = Optional.of(new GhciSession());
        } catch (HaskellException e) {
            this.ghci = Optional.empty();
        }

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handlePress);
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleDrag);
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleRelease);
        this.addEventHandler(ScrollEvent.SCROLL, this::handleScroll);

        this.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKey);

        /* Run the Inspector window. */
        Platform.runLater(() -> {
            inspector = new InspectorWindow(this);
            inspector.blockProperty().bind(this.selectedBlock);
        });
    }

    private void handleKey(KeyEvent keyEvent) {
        int dist = 100;

        switch (keyEvent.getCode()) {
            case UP:     this.setTranslateY(this.getTranslateY() + dist); break;
            case DOWN:   this.setTranslateY(this.getTranslateY() - dist); break;
            case LEFT:   this.setTranslateX(this.getTranslateX() + dist); break;
            case RIGHT:  this.setTranslateX(this.getTranslateX() - dist); break;

            case H: // C&C-style
            case BACK_SPACE: // SC-style
                this.setTranslateX(0);
                this.setTranslateY(0);
                break;

            case EQUALS: this.setScale(this.getScaleX() * 1.25); break;
            case MINUS:  this.setScale(this.getScaleX() * 0.8); break;
            case DIGIT1: this.setScale(1); break;

            case Z:
                if (inspector != null) inspector.show();
                break;

            case DELETE:
                removeSelected();
                break;
        }
    }

    private void handlePress(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            offset = new Point2D(this.getTranslateX(), this.getTranslateY());
            dragStart = new Point2D(e.getScreenX(), e.getScreenY());
            dragging = true;
        } else if (e.isSecondaryButtonDown()) {
            FunctionMenu menu = new FunctionMenu(catalog, this);
            menu.relocate(e.getX(), e.getY());
            this.getChildren().add(menu);
        }
    }

    private void handleDrag(MouseEvent e) {
        if (!e.isSecondaryButtonDown()) {
            if (dragging) {
                Point2D dragCurrent = new Point2D(e.getScreenX(), e.getScreenY());
                Point2D delta = dragStart.subtract(dragCurrent);
    
                this.setTranslateX(offset.getX() - delta.getX());
                this.setTranslateY(offset.getY() - delta.getY());
            } else {
                dragStart = new Point2D(e.getScreenX(), e.getScreenY());
                dragging = true;
            }
        }
    }
    
    private void handleRelease(MouseEvent e) {
        dragging = false;
    }

    private void setScale(double scale) {
        this.setScaleX(scale);
        this.setScaleY(scale);
    }

    private void handleScroll(ScrollEvent scrollEvent) {
        /* Ignore (drop) scroll events synthesized from touches. */
        if (scrollEvent.getTouchCount() > 0) return;

        if (scrollEvent.getDeltaY() > 0) {
            zoomIn();
        } else if (scrollEvent.getDeltaY() < 0) {
            zoomOut();
        }
    }

    /**
     * @return The Env instance to be used within this CustomUIPane.
     */
    public Environment getEnvInstance() {
        return envInstance;
    }

    /**
     * Re-evaluate all display blocks.
     * This is inefficient.
     */
    public final void invalidateAll() {
        for (Node node : getChildren()) {
            if (node instanceof Block) {
                ((Block) node).invalidateConnectionState();
                ((Block) node).invalidateVisualState();
            }
        }

        if (inspector != null) inspector.update();
    }

    public Optional<Block> getSelectedBlock() {
        return selectedBlock.get();
    }

    public void setSelectedBlock(Block selectedBlock) {
        this.selectedBlock.set(Optional.ofNullable(selectedBlock));
    }

    public ObjectProperty<Optional<Block>> selectedBlockProperty() {
        return selectedBlock;
    }
    
    public boolean getErrorOccured() {
        return errorOccurred.get();
    }
    
    public void setErrorOccurred(boolean error) {
        errorOccurred.set(error);
    }
    
    public BooleanProperty errorOccurredProperty() {
        return errorOccurred;
    }

    /** Remove the given block from this UI pane, including its connections. */
    public void removeBlock(Block block) {
        Optional<Block> target = Optional.of(block);
        ArrayList<Node> toRemove = new ArrayList<>();

        for (Node node : getChildren()) {
            if (node instanceof Connection) {
                Optional<Block> in = ((Connection) node).getInputBlock();
                Optional<Block> out = ((Connection) node).getOutputBlock();

                if (in.equals(target) || out.equals(target)) {
                    toRemove.add(node);
                }
            } else if (node.equals(block)) {
                toRemove.add(node);
            }
        }

        this.getChildren().removeAll(toRemove);
    }

    /** Remove the selected block, if any. */
    private void removeSelected() {
        this.getSelectedBlock().ifPresent(this::removeBlock);
    }

    public ConnectionCreationManager getConnectionCreationManager() {
        return connectionCreationManager;
    }

    public Optional<GhciSession> getGhciSession() {
        return ghci;
    }

    public void zoomOut() {
        zoom(0.8);
    }

    public void zoomIn() {
        zoom(1.25);
    }

    private void zoom(double ratio) {
        double scale = this.getScaleX();

        /* Limit zoom to reasonable range. */
        if (scale <= 0.25 && ratio < 1) return;
        if (scale >= 8 && ratio > 1) return;

        this.setScale(scale * ratio);
        this.setTranslateX(this.getTranslateX() * ratio);
        this.setTranslateY(this.getTranslateY() * ratio);
    }

    /**
     * Removes the association of the given expression. This should be called when an expression no longer exists in the
     * tree.
     * @param expr The expression to remove.
     */
    public void removeExprToFunction(Expression expr) {
        exprToFunction.remove(expr);
    }

    /**
     * Associates the given expression with the given function block. This should be called when a new expression is
     * created and used in the tree.
     * @param expr The expression to associate.
     * @param block The function block for the expression.
     */
    public void putExprToFunction(Expression expr, FunctionBlock block) {
        exprToFunction.put(expr,block);
    }

    /**
     * Returns the function block for an expression.
     * @param expr The expression to get the function block for.
     * @return The function block for the given expression.
     */
    public FunctionBlock getExprToFunction(Expression expr) {
        return exprToFunction.get(expr);
    }

    /** Gets the file we're currently working on, if any. */
    public Optional<File> getCurrentFile() {
        return currentFile;
    }

    /**
     * Sets the file we're currently working on. Probably called from a Save
     * As/Open operation.
     */
    public void setCurrentFile(File currentFile) {
        this.currentFile = Optional.of(currentFile);
    }
}
