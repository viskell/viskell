package nl.utwente.viskell.ui.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.ConnectionCreationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an anchor of a Block that can connect to (1 or more) Connections.
 * 
 * A ConnectionAnchor has an invisible part that acts as an enlargement of the touch zone.
 * 
 * The primary Connection (if present) is the first element in getConnections().
 * This means that the oldest Connection is the primary connection.
 */
public abstract class ConnectionAnchor extends StackPane implements ComponentLoader {

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
                    inputPressed(inputId);
                    event.consume();
                } else if ((event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)
                        || event.getEventType().equals(TouchEvent.TOUCH_MOVED))
                        && this.lineInProgress) {
                    inputMoved(inputId,x,y);
                    event.consume();
                } else if ((event.getEventType().equals(MouseEvent.MOUSE_RELEASED)
                        || event.getEventType().equals(TouchEvent.TOUCH_RELEASED))
                        && this.lineInProgress) {
                    this.lineInProgress = false;
                    inputReleased(inputId,pickResult);
                    event.consume();
                }
            }
        }
        
        /**
         * Method to indicate that the Anchor with the given inputId is pressed.
         * 
         * @param inputId
         *            The id associated with the input that triggered this action.
         */
        private void inputPressed(int inputId){
            ConnectionAnchor anchor = ConnectionAnchor.this;
            if (!anchor.canAddConnection()) {
                manager.editConnection(inputId, anchor);
            } else {
                manager.buildConnectionWith(inputId, anchor);
            }
        }
        
        /**
         * Method to indicate that the input moved.
         * 
         * @param inputId
         *            The id associated with the input that triggered this action.
         * @param x
         *            The sceneX coordinate of the input.
         * @param y
         *            The sceneY coordinate of the input.
         */
        private void inputMoved(int inputId, double x, double y){
            manager.updateLine(inputId, x, y);
        }
        
        /**
         * Method to indicate that the input was released.
         * 
         * @param inputId The id associated with the input that triggered this action.
         * @param pickResult The PickResult done at the position where the input was released.
         */
        private void inputReleased(int inputId, Node pickResult){
            if (pickResult instanceof ConnectionAnchor) {
                manager.finishConnection(inputId, (ConnectionAnchor) pickResult);
            } else {
                manager.removeConnection(inputId);
            }
        }
    }

    /** The block this ConnectionAnchor belongs to. */
    private Block block;

    /** The connections this anchor has, can be empty for no connections. */
    private List<Connection> connections;
    
    /** The visual representation of the ConnectionAnchor. */
    @FXML private Shape visibleAnchor;
    
    /** The invisible part of the ConnectionAnchor (the touch zone). */
    @FXML private Shape invisibleAnchor;
    
    /** Property storing the error state. */
    private BooleanProperty errorState;
    
    /**
     * @param block
     *            The block this ConnectionAnchor belongs to.
     */
    public ConnectionAnchor(Block block) {
        this.loadFXML("ConnectionAnchor");
        
        this.block = block;
        this.errorState = new SimpleBooleanProperty(false);
        this.connections = new ArrayList<Connection>();
        
        this.errorState.addListener(this::checkError);
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
     * @param state The new error state for this ConnectionAnchor.
     */
    public void setErrorState(boolean state) {
        errorState.set(state);
    }
    
    /**
     * @return The property describing the error state of this ConnectionAnchor.
     */
    public BooleanProperty errorStateProperty() {
        return errorState;
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
     * @return The Expr this ConnectionAnchor represents (coming from a Block).
     */
    public abstract Expression getExpr();
    
    /**
     * @return Optional of the string representation of the in- or output type.
     */
    public Optional<String> getStringType() {
        try {
            Type type = getExpr().findType();
            return Optional.of(type.prettyPrint());
        } catch (HaskellException e) {
            return Optional.empty();
        }
    }
    
    /**
     * ChangeListener that will set the error state if isConnected().
     */
    public void checkError(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        for (Connection conn : getConnections()) {
            if (conn.isFullyConnected()) {
                conn.setErrorState(newValue);
            }
        }
    }
    
    /**
     * Removes the connection from its pane, first disconnecting it from its
     * anchors.
     *
     * @param connection
     *            Connection to remove from its pane.
     */
    public void removeConnection(Connection connection) {
        if (connections.contains(connection)) {
            connections.remove(connection);
            connection.remove();
        }
    }

    /**
     * Disconnects the connection from this anchor, keeping the connection on
     * its pane.
     *
     * @param connection
     *            Connection to disconnect from.
     */
    public void disconnectConnection(Connection connection) {
        if (connections.contains(connection)) {
            connections.remove(connection);
            connection.disconnect(this);
        }
    }

    /**
     * Removes all the connections this anchor has.
     */
    public void removeConnections() {
        while (!connections.isEmpty()) {
            removeConnection(connections.get(0));
        }
    }

    /**
     * Disconnects all the connections this anchor has from this anchor.
     */
    public void disconnectConnections() {
        while (!connections.isEmpty()) {
            disconnectConnection(connections.get(0));
        }
    }

    /**
     * Adds the given connection to the connections this anchor has.
     * 
     * @param connection
     *            Connection to add
     */
    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    /**
     * @return True if this anchor has 1 or more connections.
     */
    public boolean hasConnection() {
        return !connections.isEmpty();
    }

    /**
     * @return True if the primary connection is connected.
     */
    public boolean isPrimaryConnected() {
        return isFullyConnected(0);
    }

    /**
     * @param index
     *            Index of the connection to check
     * @return Whether or not the connection specified by the index is connected. False if the index is invalid.
     */
    public boolean isFullyConnected(int index) {
        return index >= 0 && index < getConnections().size() && getConnections().get(index).isFullyConnected();
    }

    /**
     * @return Whether or not this anchor allows adding an extra connection.
     */
    public abstract boolean canAddConnection();

    /**
     * This method provides a shortcut to get the anchors on the other side of
     * the Connection from this anchor.
     * 
     * @return A list of each potential opposite anchor for each Connection this
     *         anchor has.
     */
    public List<Optional<? extends ConnectionAnchor>> getOppositeAnchors() {
        List<Optional<? extends ConnectionAnchor>> list = new ArrayList<Optional<? extends ConnectionAnchor>>();
        for (Connection c : getConnections()) {
            if (c.isFullyConnected()) {
                if (c.getInputAnchor().isPresent() && c.getInputAnchor().get().equals(this)) {
                    list.add(c.getOutputAnchor());
                } else if (c.getOutputAnchor().isPresent() && c.getOutputAnchor().get().equals(this)) {
                    list.add(c.getInputAnchor());
                } else {
                    list.add(Optional.empty());
                }
            } else {
                list.add(Optional.empty());
            }
        }
        return list;
    }

    /**
     * @return Optional of the primary connection's opposite anchor.
     */
    public Optional<? extends ConnectionAnchor> getPrimaryOppositeAnchor() {
        if (!getOppositeAnchors().isEmpty()) {
            return getOppositeAnchors().get(0);
        } else {
            return Optional.empty();
        }
    }

    /**
     * @return The block this anchor belongs to.
     */
    public final Block getBlock() {
        return block;
    }

    /**
     * @return the connections this anchor is connected to.
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * @return Optional of the primary connection.
     */
    public Optional<Connection> getPrimaryConnection() {
        if (getConnections().size() > 0) {
            return Optional.of(getConnections().get(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * @return the position of the center of this anchor relative to its pane.
     */
    public Point2D getLocalCenter() {
        return new Point2D(0, 0);
    }
    
    @Override
    public String toString() {
        return String.format("%s belonging to %s", this.getClass().getSimpleName(), getBlock());
    }
}
