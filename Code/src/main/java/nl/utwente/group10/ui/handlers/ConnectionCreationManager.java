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

    public ConnectionCreationManager(CustomUIPane pane) {
        this.pane = pane;
        connections = new HashMap<Integer, Connection>();
    }

    public Connection createConnectionWith(int id, ConnectionAnchor anchor) {
        Connection newConnection = null;
        if (anchor instanceof OutputAnchor) {
            newConnection = new Connection((OutputAnchor) anchor);
        } else if (anchor instanceof InputAnchor) {
            newConnection = new Connection((InputAnchor) anchor);
        }
        pane.getChildren().add(0, newConnection);
        connections.put(id, newConnection);
        return newConnection;
    }

    public Connection finishConnection(int id, ConnectionAnchor anchor) {
        Connection connection = connections.get(id);
        if (connection != null) {
            if (CONNECTIONS_OVERRIDE_EXISTING) {
                Optional<Connection> existingConnection = anchor
                        .getConnection();
                if (existingConnection.isPresent()) {
                    existingConnection.get().disconnect();
                    pane.getChildren().remove(existingConnection.get());
                }
            }
            if (anchor.canConnect() && connection.tryAddAnchor(anchor)) {
                // Succesfully made connection.
                pane.invalidate();
            } else {
                removeConnection(id);
            }
        }
        connections.remove(id);
        return connection;
    }

    public Connection removeConnection(int id) {
        Connection connection = connections.get(id);
        connections.put(id, null);
        if (connection != null) {
            connection.disconnect();
            pane.getChildren().remove(connection);
            connections.remove(id);
        }
        return null;
    }

    public void editConnection(int id, ConnectionAnchor anchor) {
        Optional<? extends ConnectionAnchor> anchorToKeep = anchor.getOtherAnchor();
        if (anchor.isConnected() && anchorToKeep.isPresent()) {
            Connection connection = anchor.getConnection().get();
            connection.disconnect(anchor);
            connections.put(id, connection);
            pane.invalidate();
        }
    }

    public void updateLine(int id, double x, double y) {
        Point2D localPos = pane.sceneToLocal(x, y);

        if (connections.get(id) != null) {
            connections.get(id).setFreeEnds(localPos.getX(), localPos.getY());
        }
    }
}
