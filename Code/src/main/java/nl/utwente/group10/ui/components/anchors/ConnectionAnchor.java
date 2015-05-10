package nl.utwente.group10.ui.components.anchors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.lines.Connection;

/**
 * Represents an anchor of a Block that can connect to (1 or more) Connections.
 * 
 * The primary Connection (if present) is the first element in getConnections().
 * This means that the oldest Connection is the primary connection.
 */
public abstract class ConnectionAnchor extends Circle implements ComponentLoader {
    /** The pane on which this Anchor resides. */
    private CustomUIPane pane;

    /** The block this Anchor is connected to. */
    private Block block;

    /** The connections this anchor has, can be empty for no connections. */
    private List<Connection> connections;

    /**
     * @param block
     *            The block where this Anchor is connected to.
     * @param pane
     *            The pane this Anchor belongs to.
     */
    public ConnectionAnchor(Block block, CustomUIPane pane) {
        this.block = block;
        this.pane = pane;

        this.loadFXML("ConnectionAnchor");
        connections = new ArrayList<Connection>();
    }

    /**
     * @return Input or output type of the block associated with this anchor.
     */
    public abstract Type getType();

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

    /** Returns true this anchor has 1 or more connections. */
    public boolean hasConnection() {
        return !connections.isEmpty();
    }

    /**
     * @return True if the primary connection is connected.
     */
    public boolean isPrimaryConnected() {
        return isConnected(0);
    }

    /**
     * @param index
     *            Index of the connection to check
     * @return Wether or not the connection specified by the index is connected.
     *         Returns false if the index is out of bounds.
     */
    public boolean isConnected(int index) {
        return index >= 0 && index < getConnections().size() && getConnections().get(index).isConnected();
    }

    /** Wether or not this anchor allows adding an extra connection. */
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
            if (c.isConnected()) {
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
     * @return Just the primary's opposite anchor.
     */
    public Optional<? extends ConnectionAnchor> getPrimaryOppositeAnchor() {
        if (!getOppositeAnchors().isEmpty()) {
            return getOppositeAnchors().get(0);
        } else {
            return Optional.empty();
        }
    }

    /** Returns the block this anchor belongs to. */
    public final Block getBlock() {
        return block;
    }

    /** Returns the connections this anchor is connected to. */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * @return The primary connection.
     */
    public Optional<Connection> getPrimaryConnection() {
        if (getConnections().size() > 0) {
            return Optional.of(getConnections().get(0));
        } else {
            return Optional.empty();
        }
    }

    /** Returns the position of the center of this anchor relative to its pane. */
    public Point2D getCenterInPane() {
        Point2D scenePos = localToScene(getCenterX(), getCenterY());
        return getPane().sceneToLocal(scenePos);
    }

    /** Returns the pane this anchor resides on. */
    public final CustomUIPane getPane() {
        return pane;
    }

    @Override
    public String toString() {
        return String.format("%s for %s", this.getClass().getSimpleName(), getBlock());
    }
}
