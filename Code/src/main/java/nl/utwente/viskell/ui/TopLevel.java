package nl.utwente.viskell.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javafx.geometry.Bounds;
import nl.utwente.viskell.ui.components.Block;

public class TopLevel implements BlockContainer {

    /** The pane where this top level container is on */
    private final CustomUIPane parentPane;
    
    /** The set of blocks that belong to this top level */
    private final Set<Block> attachedBlocks;
    
    public TopLevel(CustomUIPane parentPane) {
        super();
        this.parentPane = parentPane;
        this.attachedBlocks = new HashSet<>();
    }

    @Override
    public Bounds getBoundsInScene() {
        return this.parentPane.localToScene(this.parentPane.getBoundsInLocal());
    }

    @Override
    public void attachBlock(Block block) {
        this.attachedBlocks.add(block);
    }

    @Override
    public void removeBlock(Block block) {
        this.attachedBlocks.remove(block);
    }

    @Override
    public Stream<Block> getAttachedBlocks() {
        return this.attachedBlocks.stream();
    }

    @Override
    public BlockContainer getParentContainer() {
        return this;
    }

}
