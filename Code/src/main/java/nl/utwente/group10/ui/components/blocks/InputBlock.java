package nl.utwente.group10.ui.components.blocks;

import java.util.List;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.anchors.InputAnchor;

public interface InputBlock {
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
     * @param input
     *            The argument of which the type is desired.
     * @return The type that the specified input argument accepts.
     */

    Type getInputSignature(int index);

    /**
     * @param input
     *            The argument of which the type is desired.
     * @return The current type given to the specified input argument.
     */

    Type getInputType(int index);

    /**
     * @return All InputAnchors of the block.
     */
    List<InputAnchor> getAllInputs();

    /**
     * @return Only the active (as specified with by the knot index) inputs.
     */
    List<InputAnchor> getActiveInputs();
}
