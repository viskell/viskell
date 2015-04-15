package nl.utwente.group10.ui.components;

import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.shape.Circle;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.FunctionBlock;

/**
 * Represent an Anchor point on either a Block or a Line Integers are currently
 * the only supported data type.
 *
 * Other data types will be supported in the future
 */
public abstract class ConnectionAnchor extends Circle {
    /** The pane on which this Anchor resides. */
    private CustomUIPane pane;

    /** The block this Anchor is connected to. */
    private Block block;

    /** The possible connection linked to this anchor */
    private Optional<Connection> connection;

    /**
     * @param block The block where this Anchor is connected to.
     * @param pane The pane this Anchor belongs to.
     * @throws IOException when the FXML definitions cannot be loaded.
     */
    public ConnectionAnchor(Block block, CustomUIPane pane) throws IOException {
        this.block = block;
        this.pane = pane;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/ConnectionAnchor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        fxmlLoader.load();
        setConnection(null);
    }


    /**
     * @return The block this anchor belongs to.
     */
    public final Block getBlock() {
        return block;
    }

    /**
     * @return The pane this anchor resides on.
     */
    public final CustomUIPane getPane() {
        return pane;
    }

    public void setConnection(Connection connection) {
        this.connection = Optional.ofNullable(connection);
        if(this.connection.isPresent() && block instanceof FunctionBlock){
        	((FunctionBlock) block).invalidate();
        }
    }

    public boolean isConnected() {
        return connection.isPresent();
    }

    public Optional<Connection> getConnection() {
        return connection;
    }

    public abstract boolean canConnect();

    /**
     * Disconnects itself from the connection
     * @param connection Connection to disconnect from.
     */
    public abstract void disconnectFrom(Connection connection);

    public Optional<ConnectionAnchor> getOtherAnchor() {
        if (isConnected()) {
            Optional<OutputAnchor> out = getConnection().get()
                    .getOutputAnchor();
            Optional<InputAnchor> in = getConnection().get().getInputAnchor();
            if (in.isPresent() && out.get().equals(this)) {
                // Re-wrap Optional to change from InputAnchor to
                // ConnectionAnchor
                return Optional.of(in.get());
            } else if (out.isPresent() && in.get().equals(this)) {
                return Optional.of(out.get());
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "ConnectionAnchor for " + getBlock();
    }
}
