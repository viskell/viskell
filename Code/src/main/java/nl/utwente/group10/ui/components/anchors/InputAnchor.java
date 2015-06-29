package nl.utwente.group10.ui.components.anchors;

import java.util.Optional;

import edu.emory.mathcs.backport.java.util.Arrays;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.input.InputBlock;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.exceptions.TypeUnavailableException;
import nl.utwente.group10.ui.handlers.AnchorHandler;

/**
 * Anchor that specifically functions as an input.
 */
public class InputAnchor extends ConnectionAnchor {
    
    private Expr connectionlessExpr;
    
    private int argumentIndex;
    
    /**
     * @param block
     *            The Block this anchor is connected to.
     * @param signature
     *            The Type signature as is accepted by this InputAnchor.
     */
    public InputAnchor(Block block) {
        super(block);
        new AnchorHandler(super.getPane().getConnectionCreationManager(), this);
        connectionlessExpr = new Ident("undefined");
    }

    /**
     * @return The expression carried by the connection connected to this
     *         anchor.
     */
    
    @Override
    public final Expr getExpr() {
        if (isPrimaryConnected()) {
            //System.out.println("InputAnchor.getExpr(), return " + getPrimaryOppositeAnchor().get().getBlock().getExpr());
            return getPrimaryOppositeAnchor().get().getBlock().getExpr();
        } else {
            //System.out.println("InputAnchor.getExpr(), return connectionlessExpr");
            return connectionlessExpr;
        }
    }
    
    @Override
    public String getStringType() {
        try {
            return getExpr().
                    getType(getPane().getEnvInstance()).prune().toHaskellType();
        } catch (HaskellException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "ERROR";
        }
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
