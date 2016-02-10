package nl.utwente.viskell.ui;

import java.util.stream.Stream;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import nl.utwente.viskell.ui.components.Block;

/**
 * A generic interface for block containers.
 */
public interface BlockContainer {

    /** Gets the bounds to be used for testing what is inside this container, transformed into the coordinate space of its scene. */
    Bounds containmentBoundsInScene();
    
    /** Attach a block to this container */
    void attachBlock(Block block);
    
    /** Detach a block from this container */
    void detachBlock(Block block);

    /** @return a stream of all block attached to this container */
    Stream<Block> getAttachedBlocks();
    
    /** Check whether this container contains the specified block */
    default boolean containsBlock(Block block) {
        return this.getAttachedBlocks().anyMatch(a -> block.equals(a));
    }
    
    /** @return the container to which this container belongs, maybe return itself if it is the outermost container */
    BlockContainer getParentContainer();

    /**
     * @return the ToplevelPane where this container is (indirectly) part of. 
     * @throws IllegalStateException
     */
    default ToplevelPane getToplevel() {
        BlockContainer cont = this;
        while (cont.getParentContainer() != cont) {
            cont = cont.getParentContainer();
        }
        
        if (cont instanceof ToplevelPane) {
            return (ToplevelPane)cont;
        }
        
        throw new IllegalStateException("Manipulating container that is not in a ToplevelPane");
    }
    
    Node asNode();
    
    /** @return Whether this container is (indirectly) contained with the other container. */
    default boolean isContainedWithin(BlockContainer other) {
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
    void expandToFit(Bounds blockBounds);
    
    /** Return the union of two Bounds, i.e. a Bound that contains both. */
    static Bounds union(Bounds a, Bounds b) {
        double left   = Math.min(a.getMinX(), b.getMinX());
        double right  = Math.max(a.getMaxX(), b.getMaxX());
        double top    = Math.min(a.getMinY(), b.getMinY());
        double bottom = Math.max(a.getMaxY(), b.getMaxY());

        return new BoundingBox(left, top, right - left, bottom - top);
    }

}
