package nl.utwente.group10.ui;

import java.util.ArrayList;
import java.util.Optional;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.ghcj.GhciSession;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.DisplayBlock;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;

/**
 * Extension of TactilePane that keeps state for the user interface.
 */
public class CustomUIPane extends TactilePane {
    private ObjectProperty<Optional<Block>> selectedBlock;
    private ConnectionCreationManager connectionCreationManager;
    private Optional<GhciSession> ghci;

    private Point2D dragStart;
    private Point2D offset;

    private Env envInstance;

    /**
     * Constructs a new instance.
     */
    public CustomUIPane() {
        this.connectionCreationManager = new ConnectionCreationManager(this);
        this.selectedBlock = new SimpleObjectProperty<>(Optional.empty());
        this.dragStart = Point2D.ZERO;
        this.offset = Point2D.ZERO;

        try {
            this.ghci = Optional.of(new GhciSession());
        } catch (GhciException e) {
            this.ghci = Optional.empty();
        }

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handlePress);
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleDrag);
        this.addEventHandler(ScrollEvent.SCROLL, this::handleScroll);

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
    
            case DELETE:
                removeSelected();
                break;
        }
    }

    private void handlePress(MouseEvent mouseEvent) {
        offset = new Point2D(this.getTranslateX(), this.getTranslateY());
        dragStart = new Point2D(mouseEvent.getScreenX(), mouseEvent.getScreenY());
    }

    private void handleDrag(MouseEvent mouseEvent) {
        Point2D dragCurrent = new Point2D(mouseEvent.getScreenX(), mouseEvent.getScreenY());
        Point2D delta = dragStart.subtract(dragCurrent);

        this.setTranslateX(offset.getX() - delta.getX());
        this.setTranslateY(offset.getY() - delta.getY());
    }

    private void setScale(double scale) {
        this.setScaleX(scale);
        this.setScaleY(scale);
    }

    private void handleScroll(ScrollEvent scrollEvent) {
        double scale = this.getScaleX();
        double ratio = 1.0;

        if (scrollEvent.getDeltaY() > 0 && scale < 8) {
            ratio = 1.25;
        } else if (scrollEvent.getDeltaY() < 0 && scale > 0.5) {
            ratio = 0.8;
        }

        this.setScale(scale * ratio);
        this.setTranslateX(this.getTranslateX() * ratio);
        this.setTranslateY(this.getTranslateY() * ratio);

    }

    public Env getEnvInstance() {

        if (envInstance == null) {
            try {
                // envInstance = new HaskellCatalog().asEnvironment();
                return new HaskellCatalog().asEnvironment();
            } catch (CatalogException e) {
                // TODO Think of something smart to do when this happens.
                e.printStackTrace();
            }
        }
        return envInstance;
    }

    /**
     * Re-evaluate all display blocks.
     */
    public final void invalidate() {
        for (Node node : getChildren()) {
            if (node instanceof Block) {
                ((Block) node).invalidate();
            }
        }
    }

    public final void errorAll() {
        for (Node node : getChildren()) {
            if (node instanceof Block) {
                ((Block) node).error();
            } else if (node instanceof Connection) {
                ((Connection) node).error();
            }
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
}
