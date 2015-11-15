package nl.utwente.viskell.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.ui.components.Block;
import nl.utwente.viskell.ui.components.InputAnchor;

import java.io.File;
import java.util.Optional;

/**
 * Extension of TactilePane that keeps state for the user interface.
 */
public class CustomUIPane extends TactilePane {
    private ObjectProperty<Optional<Block>> selectedBlock;
    private ConnectionCreationManager connectionCreationManager;
    
    private Optional<GhciSession> ghci;
    private InspectorWindow inspector;
    private PreferencesWindow preferences;

    private Point2D dragStart;
    private Point2D offset;
    
    /** Boolean to indicate that a drag (pan) action has started, yet not finished. */
    private boolean dragging;

    private HaskellCatalog catalog;
    private Environment envInstance;

    /** The File we're currently working on, if any. */
    private Optional<File> currentFile;

    /**
     * Constructs a new instance.
     */
    public CustomUIPane(HaskellCatalog catalog) {
        this.connectionCreationManager = new ConnectionCreationManager(this);
        this.selectedBlock = new SimpleObjectProperty<>(Optional.empty());
        this.dragStart = Point2D.ZERO;
        this.offset = Point2D.ZERO;
        this.catalog = catalog;
        this.envInstance = catalog.asEnvironment();

        try {
            this.ghci = Optional.of(new GhciSession());
        } catch (HaskellException e) {
            this.ghci = Optional.empty();
        }

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handlePress);
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleDrag);
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleRelease);
        this.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKey);
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
                showInspector();
                break;

            case DELETE:
                removeSelected();
                break;
            default:
                break;
        }
    }

    public void showInspector() {
        if (inspector == null) {
            inspector = new InspectorWindow(this);
        }

        inspector.show();
    }

    public void showPreferences() {
        if (preferences == null) {
            preferences = new PreferencesWindow(this);
        }

        preferences.show();
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
                ((Block) node).invalidateVisualState();
            }
        }

        if (inspector != null) {
            inspector.update();
        }
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
    
    /** Remove the given block from this UI pane, including its connections. */
    public void removeBlock(Block block) {
        for (InputAnchor in : block.getAllInputs()) {
            in.removeConnections();
        }
        
        block.getAllOutputs().stream().forEach(output -> output.removeConnections());
        this.getChildren().removeAll(block);
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
        if (scale <= 0.2 && ratio < 1) return;
        if (scale >= 3 && ratio > 1) return;

        this.setScale(scale * ratio);
        this.setTranslateX(this.getTranslateX() * ratio);
        this.setTranslateY(this.getTranslateY() * ratio);
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
