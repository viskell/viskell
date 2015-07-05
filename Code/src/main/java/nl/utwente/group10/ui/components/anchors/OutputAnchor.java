package nl.utwente.group10.ui.components.anchors;

import java.util.Optional;

import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.output.OutputBlock;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.exceptions.TypeUnavailableException;
import nl.utwente.group10.ui.handlers.AnchorHandler;

/**
 * Anchor that specifically functions as an output.
 */
public class OutputAnchor extends ConnectionAnchor {
    /**
     * @param block
     *            The block this Anchor is connected to.
     * @param signature
     *            The Type signature as is accepted by this InputAnchor.
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

    
    @Override
    public Expr getExpr() {
        return getBlock().getExpr();
    }
    
    /*
    @Override
    public Type getType() {
        if (getBlock() instanceof OutputBlock) {
            return ((OutputBlock) getBlock()).getOutputType();
        } else {
            throw new TypeUnavailableException();
        }
    }
    */
}
