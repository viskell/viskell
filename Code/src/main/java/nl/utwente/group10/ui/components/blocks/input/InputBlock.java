package nl.utwente.group10.ui.components.blocks.input;

import java.util.List;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.anchors.InputAnchor;

/**
 * Interface for Blocks that have inputs.
 */
public interface InputBlock {
    /**
     * @param index
     *            The index of the argument of which the signature is desired.
     * @return The signature of the argument as specified by the given index.
     */
    Type getInputSignature(int index);

    /**
     * @param index
     *            The index of the argument of which the Type is desired.
     * @return The Type of the argument as specified by the given index.
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
