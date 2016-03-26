package nl.utwente.viskell.ui.components;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.StackPane;
import nl.utwente.viskell.haskell.type.Type;
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
    public interface Target {
        /** @return the connection anchor directly related to the Target object. */
        ConnectionAnchor getAssociatedAnchor();
    }
    
    /** The connection being drawn starting from this anchor, or null if none. */
    private DrawWire wireInProgress;

    /** The wire we temporarily redirect mouse events to, or null if that isn't required */
    private DrawWire eventRedirectionTarget;
    
    /** The block this ConnectionAnchor belongs to. */
    protected Block block;

    /**
     * @param block The block this ConnectionAnchor belongs to.
     */
    public ConnectionAnchor(Block block) {
        this.block = block;
        this.wireInProgress = null;
        this.eventRedirectionTarget = null;

        this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePress);
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (this.eventRedirectionTarget != null && !event.isSynthesized()) {
                this.eventRedirectionTarget.handleMouseDrag(event);
            }
            event.consume();
        });
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (this.eventRedirectionTarget != null && !event.isSynthesized()) {
                this.eventRedirectionTarget.handleMouseRelease(event);
                this.eventRedirectionTarget = null;
            }
            event.consume();
        });

        this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::handleTouchPress);
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
     * Make this anchor visually react to a draw wire getting nearby.  
     * @param goodness 0 is neutral, negative is error causing, and positive is an connectable wire.
     */
    protected abstract void setNearbyWireReaction(int goodness);

    /**
     * @return the wire is being drawn from this connection anchor, or null if none.
     */
    public DrawWire getWireInProgress() {
        return this.wireInProgress;
    }

    /**
     * @param wire is being drawn from this connection anchor, or null if the drawing has finished/failed.
     */
    protected void setWireInProgress(DrawWire wire) {
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

    public abstract Type getFreshType(); 
    
    private void handleMousePress(MouseEvent event) {
        if (this.wireInProgress == null && this.eventRedirectionTarget == null && !event.isSynthesized()) {
            this.eventRedirectionTarget = DrawWire.initiate(this, null);
        }
        event.consume();
    }

    private void handleTouchPress(TouchEvent event) {
        if (this.wireInProgress == null) {
            DrawWire.initiate(this, event.getTouchPoint());
        }
        event.consume();
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
