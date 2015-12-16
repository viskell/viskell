package nl.utwente.viskell.ui.components;

import nl.utwente.viskell.ui.BlockContainer;


/** A Block container within another block with possible outputs and inputs. */
public interface WrappedContainer extends BlockContainer {

    /** Set fresh types in all anchors of this lambda for the next typechecking cycle. */
    public void refreshAnchorTypes();

    /**
     * Handle the expression and types changes caused by modified connections or values.
     * Also propagate the changes through internal connected blocks, and then outwards.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    public void handleConnectionChanges(boolean finalPhase);

    /** Move the attached blocks with the specified offset */
    default void moveNodes(double dx, double dy) {
        this.getAttachedBlocks().forEach(node -> node.relocate(node.getLayoutX()+dx, node.getLayoutY()+dy));
    }
    
    /** Delete all associations of this container with others in preparation of deletion, including all connections */
    public void deleteAllLinks();
    
}
