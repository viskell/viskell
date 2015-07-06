package nl.utwente.group10.ui.components.blocks.input;

import java.util.List;

import com.google.common.collect.ImmutableList;
import nl.utwente.group10.ui.components.anchors.InputAnchor;

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
