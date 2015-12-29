package nl.utwente.viskell.ui.components;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.StackPane;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.ToplevelPane;
import nl.utwente.viskell.ui.serialize.Bundleable;

/**
 * Represents an anchor of a Block that can connect to (1 or more) Connections.
 * 
 * A ConnectionAnchor has an invisible part that acts as an enlargement of the touch zone.
 */
public abstract class ConnectionAnchor extends StackPane implements ComponentLoader, Bundleable {

    /** Helper interface for finding the associated connection anchor on release a wire onto something. */
    public static interface Target {
        /** @return the connection anchor directly related to the Target object. */
        public ConnectionAnchor getAssociatedAnchor();
    }
    
    /** The connection being drawn starting from this anchor, or null if none. */
    private DrawWire wireInProgress;

    /** The block this ConnectionAnchor belongs to. */
    protected final Block block;

    /**
     * @param block The block this ConnectionAnchor belongs to.
     */
    public ConnectionAnchor(Block block) {
        this.block = block;

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePress);
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (this.wireInProgress != null && !event.isSynthesized()) {
                this.wireInProgress.handleMouseDrag(event);
            }
        });
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (this.wireInProgress != null && !event.isSynthesized()) {
                this.wireInProgress.handleMouseRelease(event);
            }
        });

        this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::handleTouchPress);
    }

    /**
     * @param active The new active state for this ConnectionAnchor.
     */
    public void toggleActiveState(boolean active) {
        if (!active) {
            this.removeConnections();
        }
    }

    /**
     * Removes all the connections this anchor has.
     */
    public abstract void removeConnections();

    /**
     * @return True if this anchor has 1 or more connections.
     */
    public abstract boolean hasConnection();


    /** @return the location of where to attach wire in the coordinates of the toplevel pane. */
    public abstract Point2D getAttachmentPoint();
    
    /**
     * @param wire is being drawn from this connection anchor, or null if the drawing has finished/failed.
     */
    public void setWireInProgress(DrawWire wire) {
        this.wireInProgress = wire;
    }
    
    /**
     * @return The inner most block container associated with this anchor
     */
    public abstract BlockContainer getContainer();

    /** 
     * Handle the Connection changes for the Block this anchor is attached to.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    protected void handleConnectionChanges(boolean finalPhase) {
        this.block.handleConnectionChanges(finalPhase);
    }

    private void handleMousePress(MouseEvent event) {
        if (this.wireInProgress == null && !event.isSynthesized()) {
            this.setWireInProgress(DrawWire.initiate(this, null));
            event.consume();
        }
    }

    private void handleTouchPress(TouchEvent event) {
        if (this.wireInProgress == null) {
            this.setWireInProgress(DrawWire.initiate(this, event.getTouchPoint()));
            event.consume();
        }
    }

    @Override
    public String toString() {
        return String.format("%s belonging to %s", this.getClass().getSimpleName(), this.block);
    }

    /** @return the UIPane of the attached block. */
    public ToplevelPane getPane() {
        return this.block.getToplevel();
    }

}
