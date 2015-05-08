package nl.utwente.group10.ui.components.blocks;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;

public interface OutputBlock {

    /**
     * @return the output Anchor for this Block
     */
    public OutputAnchor getOutputAnchor();

    /*
     * Signature = non unified type, ie: a->b
     *
     * (Current)Type = unified type, ie Int -> Float (This can still have
     * signature a->b)
     *
     * These are not the same, but are related. The Type has to conform to the
     * signature.
     */

    /**
     * @return The current output type of the block.
     */
    Type getOutputType();

    Type getOutputType(Env env);

    /**
     * @return The output type as specified by the function's signature.
     */
    Type getOutputSignature();

    Type getOutputSignature(Env env);
}
