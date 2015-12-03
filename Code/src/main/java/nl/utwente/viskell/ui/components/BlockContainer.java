package nl.utwente.viskell.ui.components;

import javafx.geometry.Bounds;

/**
 * A generic interface for block containers with possible outputs and inputs.
 */
public interface BlockContainer {

    /** Set fresh types in all anchors of this lambda for the next typechecking cycle. */
    public void refreshAnchorTypes();

    /**
     * Handle the expression and types changes caused by modified connections or values.
     * Also propagate the changes through internal connected blocks, and then outwards.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    public void handleConnectionChanges(boolean finalPhase);

    /** Gets the value of the property boundsInLocal. */
    public Bounds getBoundsInLocal();
    
    /** Transforms a point from the local coordinate space of this Node into the coordinate space of its scene. */
    public Bounds localToScene(Bounds bounds);
    
    /** Attach a block to this container */
    public void attachBlock(Block block);
    
    /** Detach a block from this container */
    public boolean detachBlock(Block block);
    
    /** Check whether this container contains the specified block */
    public boolean containsBlock(Block block);
    
}
