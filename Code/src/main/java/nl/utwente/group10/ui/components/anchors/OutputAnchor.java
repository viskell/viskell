package nl.utwente.group10.ui.components.anchors;

import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.handlers.AnchorHandler;

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
    public Expr getExpr() {
        return getBlock().getExpr();
    }
}
