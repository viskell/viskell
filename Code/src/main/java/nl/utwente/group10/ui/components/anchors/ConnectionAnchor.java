package nl.utwente.group10.ui.components.anchors;

import java.util.Optional;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.lines.Connection;

/**
 * Represent an Anchor point on either a Block or a Line Integers are currently
 * the only supported data type.
 * <p>
 * Other data types will be supported in the future.
 * </p>
 */
public abstract class ConnectionAnchor extends Circle implements ComponentLoader {
    /** The pane on which this Anchor resides. */
    private CustomUIPane pane;

    /** The block this Anchor is connected to. */
    private Block block;

    /** The possible connection linked to this anchor */
    private Optional<Connection> connection;

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
        setConnection(null);
    }

    /**
     * @return Input or output type of the block associated with this anchor.
     */
    public abstract Type getType();

    /**
     * Disconnects the anchor from the connection
     *
     * @param connection
     *            Connection to disconnect from.
     */
    public abstract void removeConnection(Connection connection);

    /**
     * Set the connection this anchor is connected to.
     *
     * @param connection
     */
    public void setConnection(Connection connection) {
        this.connection = Optional.ofNullable(connection);
    }

    /** Returns true if the anchor is connected to a connection. */
    public boolean hasConnection() {
        return connection.isPresent();
    }

    /**
     * @return True if this ConnectionAnchor is connected to a Connection and
     *         that connection is fully connected.
     */
    public boolean isConnected() {
        return hasConnection() && getConnection().get().isConnected();
    }

    /** Returns true if this anchor can connect to a connection. */
    public abstract boolean canConnect();

    /**
     * This method provides a shortcut to get the anchor on the other side of the Connection from this anchor.
     * @return Optional.empty if this anchor does not have a Connection, or that Connection is not connected on both sides.
     *         Else it returns an Optional containing the other anchor.
     */
    public Optional<? extends ConnectionAnchor> getOtherAnchor() {
        if (hasConnection()) {
            Optional<OutputAnchor> out = getConnection().get()
                    .getOutputAnchor();
            Optional<InputAnchor> in = getConnection().get().getInputAnchor();
            if (out.isPresent() && out.get().equals(this)) {
                return in;
            } else if (in.isPresent() && in.get().equals(this)) {
                return out;
            }
        }
        return Optional.empty();
    }

    /** Returns the block this anchor belongs to. */
    public final Block getBlock() {
        return block;
    }

    /** Returns the connection this anchor is connected to. (if any) */
    public Optional<Connection> getConnection() {
        return connection;
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
