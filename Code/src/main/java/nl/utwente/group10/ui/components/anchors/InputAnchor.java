package nl.utwente.group10.ui.components.anchors;

import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Hole;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.handlers.AnchorHandler;

/**
 * ConnectionAnchor that specifically functions as an input.
 */
public class InputAnchor extends ConnectionAnchor {
    /** The expression to return when there is no connection. */
    private Expr connectionlessExpr;
    
    /**
     * @param block
     *            The Block this anchor is connected to.
     */
    public InputAnchor(Block block) {
        super(block);
        new AnchorHandler(super.getBlock().getPane().getConnectionCreationManager(), this);
        connectionlessExpr = new Hole();
    }

    /**
     * @return The expression carried by the connection connected to this
     *         anchor.
     */
    @Override
    public final Expr getExpr() {
        if (isPrimaryConnected()) {
            return getPrimaryOppositeAnchor().get().getBlock().getExpr();
        } else {
            return connectionlessExpr;
        }
    }
    
    @Override
    public void disconnectConnection(Connection connection) {
        connectionlessExpr = new Hole();
        super.disconnectConnection(connection);
    }

    @Override
    public boolean canAddConnection() {
        // InputAnchors only support 1 connection;
        return !hasConnection();
    }

    @Override
    public String toString() {
        return "InputAnchor for " + getBlock();
    }
    
}
