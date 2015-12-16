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
    
    /** Remove a block from this container */
    public void removeBlock(Block block);

    /** @return a stream of all block attached to this container */
    public Stream<Block> getAttachedBlocks();
    
    /** Check whether this container contains the specified block */
    public default boolean containsBlock(Block block) {
        return this.getAttachedBlocks().anyMatch(a -> block.equals(a));
    }
    
    /** @return the container to which this container belongs, maybe return itself if it is the outermost container */
    public BlockContainer getParentContainer();

}
