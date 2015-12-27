package nl.utwente.viskell.ui;

import java.util.stream.Stream;

import javafx.geometry.Bounds;
import nl.utwente.viskell.ui.components.Block;

/**
 * A generic interface for block containers.
 */
public interface BlockContainer {

    /** Gets the bounds of this container transformed into the coordinate space of its scene. */
    public Bounds getBoundsInScene();
    
    /** Attach a block to this container */
    public void attachBlock(Block block);
    
    /** Detach a block from this container */
    public void detachBlock(Block block);

    /** @return a stream of all block attached to this container */
    public Stream<Block> getAttachedBlocks();
    
    /** Check whether this container contains the specified block */
    public default boolean containsBlock(Block block) {
        return this.getAttachedBlocks().anyMatch(a -> block.equals(a));
    }
    
    /** @return the container to which this container belongs, maybe return itself if it is the outermost container */
    public BlockContainer getParentContainer();

    /** @return Whether this container is (indirectly) contained with the other container. */
    public default boolean isContainedWithin(BlockContainer other) {
        if (this == other) {
            return true;
        }
        
        BlockContainer target = this;
        while (target.getParentContainer() != target) {
            target = target.getParentContainer();
            if (target == other) {
                return true;
            }
        }
            
       return false;
    }
    
    /**
     * Grows the bounds of this container to fit the given additional bounds.
     * @param blockBounds of the Block that needs to fit in the container.
     */
    public void expandToFit(Bounds blockBounds);
}
