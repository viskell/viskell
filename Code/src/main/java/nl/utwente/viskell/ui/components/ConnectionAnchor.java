package nl.utwente.viskell.ui.components;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.ConnectionCreationManager;
import nl.utwente.viskell.ui.serialize.Bundleable;

/**
 * Represents an anchor of a Block that can connect to (1 or more) Connections.
 * 
 * A ConnectionAnchor has an invisible part that acts as an enlargement of the touch zone.
 */
public abstract class ConnectionAnchor extends StackPane implements ComponentLoader, Bundleable {

    /**
     * Handler class that reacts to user inputs on ConnectionAnchors to be able to
     * create, edit and drag Connections.
     * TODO merge this inner class into ConnectionAnchor once ConnectionCreationManager is removed for a proper solution 
     */
    private class AnchorHandler implements EventHandler<InputEvent> {
        /** The ConnectionCreationManager to which this AnchorHandler belongs. */
        private ConnectionCreationManager manager;
        
        /** Whether a new connection line is partially created from this anchor */
        private boolean lineInProgress;

        /**
         * Constructs a new AnchorHandler
         * 
         * @param manager The ConnectionCreationManager to which this AnchorHandler belongs.
         */
        public AnchorHandler(ConnectionCreationManager manager) {
            this.manager = manager;
            ConnectionAnchor anchor = ConnectionAnchor.this;
            this.lineInProgress = false;

            anchor.addEventFilter(MouseEvent.MOUSE_PRESSED, this);
            anchor.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
            anchor.addEventFilter(MouseEvent.MOUSE_RELEASED, this);

            anchor.addEventFilter(TouchEvent.TOUCH_PRESSED, this);
            anchor.addEventFilter(TouchEvent.TOUCH_MOVED, this);
            anchor.addEventFilter(TouchEvent.TOUCH_RELEASED, this);
        }

        @Override
        public void handle(InputEvent event) {
            Node pickResult = null;
            int inputId = ConnectionCreationManager.INPUT_ID_NONE;
            double x = 0;
            double y = 0;

            /* Extract input information for either mouse or touch. */
            if (event instanceof MouseEvent && !((MouseEvent) event).isSynthesized()) {
                MouseEvent mEvent = ((MouseEvent) event);
                pickResult = mEvent.getPickResult().getIntersectedNode().getParent();
                inputId = ConnectionCreationManager.INPUT_ID_MOUSE;
                x = mEvent.getSceneX();
                y = mEvent.getSceneY();
            } else if (event instanceof TouchEvent) {
                TouchPoint tp = ((TouchEvent) event).getTouchPoint();
                pickResult = tp.getPickResult().getIntersectedNode().getParent();
                inputId = tp.getId();
                x = tp.getSceneX();
                y = tp.getSceneY();
            }

            /* Use the input information to call the appropriate method. */
            if ((inputId == ConnectionCreationManager.INPUT_ID_MOUSE || inputId > 0)) {
                if ((event.getEventType().equals(MouseEvent.MOUSE_PRESSED)
                        || event.getEventType().equals(TouchEvent.TOUCH_PRESSED))
                        && !this.lineInProgress) {

                    this.lineInProgress = true;
                    manager.initiateWireFrom(inputId, ConnectionAnchor.this);
                    event.consume();
                } else if ((event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)
                        || event.getEventType().equals(TouchEvent.TOUCH_MOVED))
                        && this.lineInProgress) {

                    manager.updateLine(inputId, x, y);
                    event.consume();
                } else if ((event.getEventType().equals(MouseEvent.MOUSE_RELEASED)
                        || event.getEventType().equals(TouchEvent.TOUCH_RELEASED))
                        && this.lineInProgress) {

                    if (pickResult instanceof ConnectionAnchor) {
                        manager.finishConnection(inputId, (ConnectionAnchor) pickResult);
                    } else {
                        manager.removeWire(inputId);
                    }
                    
                    this.lineInProgress = false;
                    event.consume();
                }
            }
        }
        
    }

    /** The block this ConnectionAnchor belongs to. */
    protected final Block block;
    
    /** The local type of this anchor */
    private Type type;

    /** The visual representation of the ConnectionAnchor. */
    @FXML private Shape visibleAnchor;
    
    /** The invisible part of the ConnectionAnchor (the touch zone). */
    @FXML private Shape invisibleAnchor;
    
    /**
     * @param block The block this ConnectionAnchor belongs to.
     */
    public ConnectionAnchor(Block block) {
        this.loadFXML("ConnectionAnchor");
        this.block = block;
        this.type = TypeScope.unique("???");
        this.new AnchorHandler(block.getPane().getConnectionCreationManager());
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
     * @return The Shape that is the visible part of the ConnectionAnchor.
     */
    public Shape getVisibleAnchor() {
        return visibleAnchor;
    }
    
    /**
     * @return The Shape that is the invisible part (touch zone) of this ConnectionAnchor.
     */
    public Shape getInvisibleAnchor() {
        return invisibleAnchor;
    }

    /**
     * @return the local type of this anchor
     */
    public Type getType() {
        return this.type;
    }
    
    /**
     * @param type the local type of this anchor
     */
    public void setType(Type type) {
        this.type = type;
    }
    
    /**
     * @return the string representation of the in- or output type.
     */
    public String getStringType() {
        return this.type.prettyPrint();
    }
    
    /**
     * Removes all the connections this anchor has.
     */
    public abstract void removeConnections();

    /**
     * @return True if this anchor has 1 or more connections.
     */
    public abstract boolean hasConnection();

    /** Initiate connection changes at the Block this anchor is attached to. */
    public void initiateConnectionChanges() {
        this.block.initiateConnectionChanges();
    }
    
    /** 
     * Handle the Connection changes for the Block this anchor is attached to.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    protected void handleConnectionChanges(boolean finalPhase) {
        this.block.handleConnectionChanges(finalPhase);
    }
    
    @Override
    public String toString() {
        return String.format("%s belonging to %s", this.getClass().getSimpleName(), this.block);
    }
}
