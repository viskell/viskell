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

public class ConnectionCreationManager {

    CustomUIPane pane;

    /**
     * Touch points have an ID associated with each specific touch point, this
     * is the ID associated with the Mouse.
     */
    public static final Integer MOUSE_ID = 0;
    /**
     * Maps an (Touch or Mouse) ID to a line, used to keep track of what touch
     * point is dragging what line.
     */
    private Map<Integer, Connection> connections;

    /**
     * When set to true, the connection to create can override existing
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
    
    public ConnectionCreationManager(CustomUIPane pane) {
        this.pane = pane;
        connections = new HashMap<Integer, Connection>();
    }

    public Connection createConnectionWith(int id, ConnectionAnchor anchor) {
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

    public Connection finishConnection(int id, ConnectionAnchor anchor) {
        Connection connection = connections.get(id);
        if (connection != null) {
            if (CONNECTIONS_OVERRIDE_EXISTING && !anchor.canAddConnection()) {
                anchor.removeConnections();
            }
            
            if (anchor.canAddConnection() && connection.tryAddAnchor(anchor)) {
                // Succesfully made connection.
            } else {
                removeConnection(id);
            }
        }
        connections.remove(id);
        return connection;
    }

    public void removeConnection(int id) {
        Connection connection = connections.get(id);
        connections.put(id, null);
        if (connection != null) {
            connection.disconnect();
            pane.getChildren().remove(connection);
            connections.remove(id);
        }
    }

    public void editConnection(int id, ConnectionAnchor anchor) {
        Optional<? extends ConnectionAnchor> anchorToKeep = anchor.getPrimaryOppositeAnchor();
        if (anchor.isPrimaryConnected()) {
            Connection connection = anchor.getPrimaryConnection().get();
            connection.disconnect(anchor);
            System.out.println("editConnection() "+connection);
            connections.put(id, connection);
        }
    }

    public void updateLine(int id, double x, double y) {
        Point2D localPos = pane.sceneToLocal(x, y);
        System.out.println("updateLine(): "+connections.get(id));
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
    public static void nextConnectionState(){
        connectionState++;
    }
}
