package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Interface for Blocks that have inputs.
 */
public interface InputBlock {

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
