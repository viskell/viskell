package nl.utwente.group10.ui.components.blocks;

import java.util.List;

import com.google.common.collect.ImmutableList;
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
    Type getInputSignature(InputAnchor input);

    default Type getInputSignature(int index) {
        return getInputSignature(getAllInputs().get(index));
    }

    /**
     * @param input
     *            The argument of which the type is desired.
     * @return The current type given to the specified input argument.
     */
    default Type getInputType(InputAnchor input) {
        return getInputSignature(input);
    }

    default Type getInputType(int index) {
        return getInputSignature(index);
    }

    /**
     * @return All inputs of the block.
     */
    default List<InputAnchor> getAllInputs() {
        return ImmutableList.of();
    }

    /**
     * @return Only the active (as specified with the bowtie) inputs.
     */
    default List<InputAnchor> getActiveInputs() {
        return getAllInputs();
    }

    /**
     * @return The index the specified anchor has (in getInputs())
     */
    default int getInputIndex(InputAnchor anchor) {
        return getAllInputs().indexOf(anchor);
    }
}
