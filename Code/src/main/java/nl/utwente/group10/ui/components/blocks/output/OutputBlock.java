package nl.utwente.group10.ui.components.blocks.output;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;

/**
 * Interface for Blocks that have an output.
 */
public interface OutputBlock {

    /**
     * @return the output Anchor for this Block
     */
    public OutputAnchor getOutputAnchor();
}
