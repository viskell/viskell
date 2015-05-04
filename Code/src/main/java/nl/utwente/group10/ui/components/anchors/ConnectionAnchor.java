package nl.utwente.group10.ui.components.anchors;

import java.io.IOException;
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
 *
 * Other data types will be supported in the future
 */
public abstract class ConnectionAnchor extends Circle implements
        ComponentLoader {
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
     * @throws IOException
     *             when the FXML definitions cannot be loaded.
     */
    public ConnectionAnchor(Block block, CustomUIPane pane) {
        this.block = block;
        this.pane = pane;

        try {
            getFXMLLoader("ConnectionAnchor").load();
        } catch (IOException e) {
            // TODO Find a good way to handle this
            e.printStackTrace();
        }
        setConnection(null);
    }

    /**
     * @return The block this anchor belongs to.
     */
    public final Block getBlock() {
        return block;
    }

    /**
     * @return Input or output type of the block associated with this anchor.
     */
    public abstract Type getType();

    /**
     * @return The pane this anchor resides on.
     */
    public final CustomUIPane getPane() {
        return pane;
    }

    public void setConnection(Connection connection) {
        this.connection = Optional.ofNullable(connection);
    }

    /**
     * @return True if this ConnectionAnchor is connected to a Connection
     */
    public boolean isConnected() {
        return connection.isPresent();
    }

    /**
     * @return True if this ConnectionAnchor is connected to a Connection and
     *         that connection is fully connected.
     */
    public boolean isFullyConnected() {
        return isConnected() && getConnection().get().isConnected();
    }

    public Optional<Connection> getConnection() {
        return connection;
    }

    public abstract boolean canConnect();

    /**
     * Disconnects itself from the connection
     * 
     * @param connection
     *            Connection to disconnect from.
     */
    public abstract void disconnectFrom(Connection connection);

    public Optional<? extends ConnectionAnchor> getOtherAnchor() {
        if (isConnected()) {
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

    /** @return the position of the center of this Anchor relative to its pane. */
    public Point2D getCenterInPane() {
        Point2D scenePos = localToScene(getCenterX(), getCenterY());
        return getPane().sceneToLocal(scenePos);
    }

    @Override
    public String toString() {
        return String.format("%s for %s", this.getClass().getSimpleName(),
                getBlock());
    }
}
