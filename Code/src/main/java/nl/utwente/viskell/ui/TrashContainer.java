package nl.utwente.viskell.ui;

import java.util.stream.Stream;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import nl.utwente.viskell.ui.components.Block;

/** A dummy Block container for deleted blocks */
public final class TrashContainer implements BlockContainer {

    /** The instance of the TrashContainer */
    public static TrashContainer instance = new TrashContainer();
    
    private TrashContainer() {
        super();
    }
    
    @Override
    public Bounds containmentBoundsInScene() {
        return new BoundingBox(0, 0, 0, 0);
    }

    @Override
    public void attachBlock(Block block) {
    }

    @Override
    public void detachBlock(Block block) {
    }

    @Override
    public Stream<Block> getAttachedBlocks() {
        return Stream.of();
    }

    @Override
    public BlockContainer getParentContainer() {
        return this;
    }

    @Override
    public Node asNode() {
        return new Region();
    }

    @Override
    public void expandToFit(Bounds blockBounds) {
        // everything fits in the trash
    }

}
