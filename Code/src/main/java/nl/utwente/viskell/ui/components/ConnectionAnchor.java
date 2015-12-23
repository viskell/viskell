package nl.utwente.viskell.ui.components;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.StackPane;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.serialize.Bundleable;

/**
 * Represents an anchor of a Block that can connect to (1 or more) Connections.
 * 
 * A ConnectionAnchor has an invisible part that acts as an enlargement of the touch zone.
 */
public abstract class ConnectionAnchor extends StackPane implements ComponentLoader, Bundleable {

    /** The connection being drawn starting from this anchor, or null if none. */
    protected DrawWire wireInProgess;

    /** The block this ConnectionAnchor belongs to. */
    protected final Block block;
    
    /**
     * @param block The block this ConnectionAnchor belongs to.
     */
    public ConnectionAnchor(Block block) {
        this.block = block;
        
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePress);
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDrag);
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseRelease);
        
        this.addEventHandler(TouchEvent.TOUCH_PRESSED, this::handleTouchPress);
        this.addEventHandler(TouchEvent.TOUCH_MOVED, this::handleTouchMove);
        this.addEventHandler(TouchEvent.TOUCH_RELEASED, this::handleTouchRelease);
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
     * @return the local type of this anchor
     */
    public abstract Type getType();
    
    /**
     * @return the string representation of the in- or output type.
     */
    public final String getStringType() {
        return this.getType().prettyPrint();
    }
    
    /**
     * Removes all the connections this anchor has.
     */
    public abstract void removeConnections();

    /**
     * @return True if this anchor has 1 or more connections.
     */
    public abstract boolean hasConnection();

    
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
    	if (this.wireInProgess == null && !event.isSynthesized()) {
    		this.wireInProgess = DrawWire.initiate(this.block.getPane(), this, DrawWire.INPUT_ID_MOUSE);
    		event.consume();
    	}
    }

    private void handleTouchPress(TouchEvent event) {
    	if (this.wireInProgess == null) {
    		int touchID = event.getTouchPoint().getId();
    		this.wireInProgess = DrawWire.initiate(this.block.getPane(), this, touchID);
    		event.consume();
    	}
    }
    
    private void handleMouseDrag(MouseEvent event) {
    	if (this.wireInProgess != null && !event.isSynthesized()) {
    		this.wireInProgess.handleMouseDrag(event);
    	}
    }
    
    private void handleMouseRelease(MouseEvent event) {
    	if (this.wireInProgess != null && !event.isSynthesized()) {
    		this.wireInProgess.handleMouseRelease(event);
    	}
    }

    private void handleTouchMove(TouchEvent event) {
    	if (this.wireInProgess != null) {
    		this.wireInProgess.handleTouchMove(event);
    	}
    }
    
    private void handleTouchRelease(TouchEvent event) {
    	if (this.wireInProgess != null) {
    		this.wireInProgess.handleTouchRelease(event);
    	}
    }

    @Override
    public String toString() {
        return String.format("%s belonging to %s", this.getClass().getSimpleName(), this.block);
    }

}
