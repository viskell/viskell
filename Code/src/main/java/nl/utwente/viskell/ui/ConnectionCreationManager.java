package nl.utwente.viskell.ui;

import javafx.geometry.Point2D;
import nl.utwente.viskell.ui.components.Connection;
import nl.utwente.viskell.ui.components.ConnectionAnchor;
import nl.utwente.viskell.ui.components.DrawWire;
import nl.utwente.viskell.ui.components.InputAnchor;

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
 * {@linkplain #removeWire(int)},<br> 
 * {@linkplain #initiateWireFrom(int, ConnectionAnchor)}.
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
    
    /** Maps an (Touch or Mouse) ID to a partial connection line */
    private Map<Integer, DrawWire> wires;

    /**
     * Constructs a new ConnectionCreationManager.
     * @param pane The CustomUIPane to which this ConnectionCreationManager belongs.
     */
    public ConnectionCreationManager(CustomUIPane pane) {
        this.pane = pane;
        this.wires = new HashMap<>();
    }

    /**
     * Creates a new partial wire given an anchor, and in case the anchor
     * cannot have more connections, existing connection will be removed. 
     * @param id The id associated with the action on which to follow up.
     * @param anchor The anchor to build a wire from.
     */
    public void initiateWireFrom(int inputId, ConnectionAnchor anchor) {
        if (anchor instanceof InputAnchor && ((InputAnchor)anchor).hasConnection()) {
            // make room for a new connection by removing existing one
            Connection conn = ((InputAnchor)anchor).getConnection().get();
            conn.remove();
            // keep the other end of old connection to initiate the new one
            this.wires.put(inputId, new DrawWire(this.pane, conn.getStartAnchor()));
        } else {
            this.wires.put(inputId, new DrawWire(this.pane, anchor));
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
        DrawWire wire = wires.get(id);
        if (wire != null) {
            Connection connection = wire.buildConnectionTo(anchor);
            // drop the wire, even if connection failed
            this.removeWire(id);
            if (connection != null) {
                connection.getStartAnchor().initiateConnectionChanges();
            }
        } else {
            throw new RuntimeException("InvalidInputId");
        }
    }

    /**
     * Completely Removes the wire associated with the given id.
     */
    public void removeWire(int id) {
        DrawWire wire = this.wires.get(id);
        if (wire != null) {
            this.wires.remove(id);
            wire.remove();
        }
    }

    /**
     * Updates the wire associated with the given action id to its new position.
     * @param id    The id associated with the action on which to follow up.
     * @param x     New X coordinate
     * @param y     New Y coordinate
     */
    public void updateLine(int id, double x, double y) {
        Point2D localPos = pane.sceneToLocal(x, y);
        if (wires.get(id) != null) {
            wires.get(id).setFreePosition(localPos);
        }
    }

}
