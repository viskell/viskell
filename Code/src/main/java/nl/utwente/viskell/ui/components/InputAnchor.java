package nl.utwente.viskell.ui.components;

import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Hole;

/**
 * ConnectionAnchor that specifically functions as an input.
 */
public class InputAnchor extends ConnectionAnchor {
    /** The expression to return when there is no connection. */
    private Expression connectionlessExpr;
    
    /**
     * @param block
     *            The Block this anchor is connected to.
     */
    public InputAnchor(Block block) {
        super(block);
        connectionlessExpr = new Hole();
    }

    /**
     * @return The expression carried by the connection connected to this anchor.
     */
    @Override
    public final Expression getExpr() {
        if (isPrimaryConnected()) {
            return getPrimaryOppositeAnchor().get().getBlock().getExpr();
        } else {
            return connectionlessExpr;
        }
    }
    
    /**
     * Gets the Expression that is connected to this, or when not connected create a fresh expression representing the open input.   
     * @return The updated expression carried by the connection connected to this anchor.
     */
    public final Expression getUpdatedExpr() {
        connectionlessExpr = new Hole();
        return getExpr();
    }

    @Override
    public boolean canAddExtraConnection() {
        // InputAnchors only support 1 connection;
        return !hasConnection();
    }

    @Override
    public String toString() {
        return "InputAnchor for " + getBlock();
    }
    
}
