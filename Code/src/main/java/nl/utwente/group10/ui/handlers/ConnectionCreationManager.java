package nl.utwente.group10.ui.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.geometry.Point2D;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.exceptions.InvalidInputIdException;

/**
 * A Manager class mainly used to keep track of multiple, possibly concurrent,
 * actions performed on Connections.
 * These actions are associated with an input id.
 * 
 * This class also provides the methods to perform actions based on user input.
 * 
 * This class also stores a ConnectionState.
 * This is an always increasing int used to check if components's (connection) states are fresh.
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

    /**
     * When set to true, the connection to be created can override existing
     * connections to a ConnectionAnchor.
     */
    public static final boolean CONNECTIONS_OVERRIDE_EXISTING = true;

    /**
     * When set to true a connection can be made with input type and output type
     * not matching.
     */
    public static final boolean CONNECTIONS_ALLOW_TYPE_MISMATCH = true;

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
     * Creates a new Connection, with the given anchor being part of it (can be
     * either start or end anchor). This Connection is still being made (a
     * second anchor still needs to be selected).
     * 
     * @param id
     *            The id associated with the action on which to follow up.
     * @param anchor
     *            The anchor to build a Connection with
     * @return The newly build Connection object.
     */
    public Connection buildConnectionWith(int id, ConnectionAnchor anchor) {
        Connection newConnection = null;
        if (anchor instanceof OutputAnchor) {
            newConnection = new Connection(pane, (OutputAnchor) anchor);
        } else if (anchor instanceof InputAnchor) {
            newConnection = new Connection(pane, (InputAnchor) anchor);
        }
        pane.getChildren().add(0, newConnection);
        connections.put(id, newConnection);
        return newConnection;
    }

    /**
     * Finishes building the Connection associated with the given ID, by giving
     * it its second anchor.
     * 
     * Throws an InvalidInputException if an invalid input id is given.
     * 
     * @param id
     *            The id associated with the action on which to follow up.
     * @param anchor
     *            The anchor to add to the connection being finished.
     * @return The finished connection.
     */
    public Connection finishConnection(int id, ConnectionAnchor anchor) {
        Connection connection = connections.get(id);
        if (connection != null) {
            if (CONNECTIONS_OVERRIDE_EXISTING && !anchor.canAddConnection()) {
                anchor.removeConnections();
            }
            
            if (anchor.canAddConnection() && connection.tryAddAnchor(anchor) && connection.isConnected()) {
                // Succesfully made connection.
            } else {
                removeConnection(id);
                return null;
            }

            connections.remove(id);
            return connection;
        }else{
            throw new InvalidInputIdException();
        }
    }

    /**
     * Completely Removes the Connection associated with the given id, that is
     * to remove the Connection from the Connections being build and from its
     * pane.
     * 
     * @param id
     *            The id associated with the action on which to follow up.
     */
    public void removeConnection(int id) {
        Connection connection = connections.get(id);
        if (connection != null) {
            connections.put(id, null);
            connection.remove();
            connections.remove(id);
        }
    }

    /**
     * Indicates that the primary connection belonging to the given anchor is
     * being edited. This means that the primary connection of the given anchor
     * will be disconnected, and stored as if the finishConnection() method was
     * not called for this connection.
     * 
     * @param id
     *            The id associated with the action on which to follow up.
     * @param anchor
     *            The anchor to start the edit operation from, this anchor will
     *            be disconnected from its connection.
     */
    public void editConnection(int id, ConnectionAnchor anchor) {
        if (anchor.isPrimaryConnected()) {
            Connection connection = anchor.getPrimaryConnection().get();
            connection.disconnect(anchor);
            connections.put(id, connection);
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
            connections.get(id).setFreeEnds(localPos.getX(), localPos.getY());
        }
    }
    
    /**
     * @return The current connection state.
     */
    public static int getConnectionState(){
        return connectionState;
    }
    
    /**
     * Go to the next connection state.
     */
    public static int nextConnectionState(){
        return ++connectionState;
    }
}
