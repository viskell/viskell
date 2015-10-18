package nl.utwente.viskell.ui.components;

import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.ui.AnchorHandler;

/**
 * Anchor that specifically functions as an output.
 */
public class OutputAnchor extends ConnectionAnchor {
    /**
     * @param block
     *            The block this Anchor is connected to.
     */
    public OutputAnchor(Block block) {
        super(block);
        // By default the invisible anchor covers an area above the visible
        // anchor (for InputAnchors), this switches that around to cover more of
        // the area under the visible anchor.
        getInvisibleAnchor().setTranslateY(getInvisibleAnchor().getTranslateY() * -1);
        new AnchorHandler(super.getBlock().getPane().getConnectionCreationManager(), this);
    }
    
    @Override
    public boolean canAddConnection() {
        // OutputAnchors can have multiple connections;
        return true;
    }

    /**
     * @return The expression carried by the block to which this anchor belongs.
     */
    @Override
    public Expression getExpr() {
        return getBlock().getExpr();
    }
}
