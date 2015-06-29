package nl.utwente.group10.ui.components.blocks.input;

import java.util.List;

import com.google.common.collect.ImmutableList;

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
    //Type getInputSignature(int index);

    /**
     * @param index
     *            The index of the argument of which the Type is desired.
     * @return The Type of the argument as specified by the given index.
     */
    Type getInputType(int index);

    /**
     * @return All InputAnchors of the block.
     */
    default List<InputAnchor> getAllInputs() {
        return ImmutableList.of();
    }

    /**
     * @return Only the active (as specified with by the knot index) inputs.
     */
    default List<InputAnchor> getActiveInputs() {
        return getAllInputs();
    }
}
