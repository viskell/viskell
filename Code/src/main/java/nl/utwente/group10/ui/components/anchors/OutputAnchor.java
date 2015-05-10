package nl.utwente.group10.ui.components.anchors;

import java.util.Optional;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.OutputBlock;
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
     * @param pane
     *            The parent pane on which this anchor resides.
     */
    public OutputAnchor(Block block, CustomUIPane pane) {
        super(block, pane);
        new AnchorHandler(pane.getConnectionCreationManager(), this);
    }

    @Override
    public boolean canAddConnection() {
        // OutputAnchors can have multiple connections;
        return true;
    }

    @Override
    public Type getType() {
        if (getBlock() instanceof OutputBlock) {
            return ((OutputBlock) getBlock()).getOutputType();
        } else {
            throw new TypeUnavailableException();
        }
    }
}
