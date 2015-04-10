package nl.utwente.group10.ui.components;

import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.handlers.InputAnchorHandler;

import java.io.IOException;
import java.util.Optional;

/**
 * Anchor that specifically functions as an input.
 */
public class InputAnchor extends ConnectionAnchor {
    /**
     * @param block The Block this anchor is connected to.
     * @param pane The parent pane this Anchor resides on.
     * @throws IOException when the FXML definitions cannot be loaded.
     */
    public InputAnchor(Block block, CustomUIPane pane) throws IOException {
        super(block, pane);
        new InputAnchorHandler(pane.getConnectionCreationManager(), this);
    }

    /**
     * @return The expression carried by the connection connected to this anchor.
     */
    public final Expr asExpr() {
        if (isConnected()) {
            return getConnection().get().getOutputAnchor().get().getBlock()
                    .asExpr();
        } else {
            return new Ident("undefined");
        }
    }

    public Optional<Connection> createConnectionFrom(OutputAnchor other) {
        if (!isConnected()) {
            new Connection(this, other);
            getPane().getChildren().add(getConnection().get());
            getPane().invalidate();
            return getConnection();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void disconnect(Connection connection) {
        assert connection.equals(getConnection().get());
        setConnection(null);
    }

    @Override
    public boolean canConnect() {
        // InputAnchors only support 1 connection;
        return !isConnected();
    }

}
