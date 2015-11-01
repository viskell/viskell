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
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.ConnectionCreationManager;
import nl.utwente.viskell.ui.serialize.Bundleable;

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
                    manager.initiateConnectionFrom(inputId, ConnectionAnchor.this);
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
                        manager.removeConnection(inputId);
                    }
                    
                    this.lineInProgress = false;
                    event.consume();
                }
            }
        }
        
    }

    /** The block this ConnectionAnchor belongs to. */
    protected final Block block;

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
        for (Connection conn : this.connections) {
            if (conn.isFullyConnected()) {
                conn.setErrorState(newValue);
            }
        }
    }
    
    /**
     * Drops the connection from this anchor
     *
     * @param connection Connection to disconnect from.
     */
    protected void dropConnection(Connection connection) {
        if (connections.contains(connection)) {
            connections.remove(connection);
        }
    }

    /**
     * Removes all the connections this anchor has.
     */
    public void removeConnections() {
        while (!connections.isEmpty()) {
            Connection connection = connections.remove(0);
            connection.remove();
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
        return index >= 0 && index < this.connections.size() && this.connections.get(index).isFullyConnected();
    }

    /**
     * @return Whether or not this anchor allows adding an extra connection.
     */
    public abstract boolean canAddExtraConnection();

    /**
    * @return the connections this anchor is connected to.
    */
    protected List<Connection> getConnections() {
        return connections;
    }

    /**
     * Attempt to get the nth connection to this anchor, if present
     * 
     * @param index this index of the connection.
     * @return Optional of the connection at index.
     */
    public Optional<Connection> getConnection(int index) {
        if (this.connections.size() > index) {
            return Optional.of(this.connections.get(index));
        }
        
        return Optional.empty();
    }

    /** Handle the Connection changes for the Block this anchor is attached to. */
    public void handleConnectionChanges() {
        this.block.handleConnectionChanges();
    }
    
    /**
     * @return the position of the center of this anchor relative to its pane.
     */
    public Point2D getLocalCenter() {
        return new Point2D(0, 0);
    }
    
    @Override
    public String toString() {
        return String.format("%s belonging to %s", this.getClass().getSimpleName(), this.block);
    }
}
