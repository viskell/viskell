package nl.utwente.viskell.ui;

import javafx.geometry.Point2D;
import nl.utwente.viskell.ui.components.Connection;
import nl.utwente.viskell.ui.components.ConnectionAnchor;

import java.util.HashMap;
import java.util.Map;

/**
 * A Manager class mainly used to keep track of multiple, possibly concurrent,
 * actions performed on Connections.
 * These actions are associated with an input id.
 * <p>
 * This class also provides the methods to perform actions based on user input.
 * Possible actions include: 
 * {@linkplain #finishConnection(int, ConnectionAnchor)},<br> 
 * {@linkplain #removeConnection(int)},<br> 
 * {@linkplain #initiateConnectionFrom(int, ConnectionAnchor)}.
 * </p>
 * <p>
 * This class also stores a ConnectionState.
 * This is an always increasing int used to check if components's (connection) states are fresh. 
 * </p>
 */
public class ConnectionCreationManager {
    /** The Pane on which this ConnectionCreationManager is active. */
    CustomUIPane pane;

    /**
     * Touch points have an ID associated with each specific touch point, this
     * is the ID associated with the Mouse.
     */
    public static final Integer INPUT_ID_MOUSE = -1;
    
    /**
     * Touch points have an ID associated with each specific touch point, this
     * is the ID associated with no input.
     */
    public static final Integer INPUT_ID_NONE = -2;
    
    /**
     * Maps an (Touch or Mouse) ID to a line, used to keep track of what touch
     * point is dragging what line.
     */
    private Map<Integer, Connection> connections;

    /** The int representing the current connection state */
    private static int connectionState = 0; 
    
    /**
     * Constructs a new ConnectionCreationManager.
     * @param pane The CustomUIPane to which this ConnectionCreationManager belongs.
     */
    public ConnectionCreationManager(CustomUIPane pane) {
        this.pane = pane;
        connections = new HashMap<Integer, Connection>();
    }

    /**
     * Creates either a temporary Connection, with the given start or end anchor,
     * or when this anchor cannot have extra connection disconnect the existing one
     * to use as editable partial connection instead. 
     * The Connection will be finalized when a matching anchor has been added.
     *
     * @param id The id associated with the action on which to follow up.
     * @param anchor The anchor to build a Connection from.
     */
    public void initiateConnectionFrom(int inputId, ConnectionAnchor anchor) {
        if (!anchor.canAddExtraConnection()) {
            // take the existing connection instead
            Connection connection = anchor.getConnection(0).get();
            connection.disconnect(anchor);
            connections.put(inputId, connection);
        } else {
            Connection newConnection = new Connection(pane, anchor);
            connections.put(inputId, newConnection);
        }
    }
    
    /**
     * Assigns the second anchor to finish the building of the Connection.
     * 
     * @param id The id associated with this action.
     * @param anchor The anchor to add to the connection being finished.
     * @throws InvalidInputException.
     */
    public void finishConnection(int id, ConnectionAnchor anchor) {
        Connection connection = connections.get(id);
        if (connection != null) {
            if (!anchor.canAddExtraConnection()) {
                anchor.removeConnections(); // push out the existing connections
            }

            connection.connectTo(anchor);
            
            if (!connection.isFullyConnected()) {
                 // failed to produce a complete connection, cancel it.
                removeConnection(id);
                return;
            }

            connections.remove(id);
        } else {
            throw new RuntimeException("InvalidInputId");
        }
    }

    /**
     * Completely Removes the Connection associated with the given id.
     */
    public void removeConnection(int id) {
        Connection connection = connections.get(id);
        if (connection != null) {
            connections.remove(id);
            connection.remove();
        }
    }

    /**
     * Updates the Connection's line associated with the given action id to its new position.
     * @param id    The id associated with the action on which to follow up.
     * @param x     New X coordinate
     * @param y     New Y coordinate
     */
    public void updateLine(int id, double x, double y) {
        Point2D localPos = pane.sceneToLocal(x, y);
        if (connections.get(id) != null) {
            connections.get(id).setFreeEnds(localPos);
        }
    }

    /**
     * Go to the next connection state.
     */
    public static int nextConnectionState(){
        return ++connectionState;
    }

}
