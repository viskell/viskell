package nl.utwente.viskell.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Shape;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.ui.components.Block;
import nl.utwente.viskell.ui.components.Connection;
import nl.utwente.viskell.ui.components.DrawWire;
import nl.utwente.viskell.ui.components.WrappedContainer;

/**
 * The core Pane that represent the programming workspace.
 * It is a layered visualization of all blocks, wires, and menu elements.
 * And represents the toplevel container of all blocks.
 */
public class ToplevelPane extends Region implements BlockContainer {
    /** bottom pane layer intended for block container such as lambda's */
    private final Pane bottomLayer;

    /** middle pane layer for ordinary blocks */
    private final Pane blockLayer;

    /** higher pane layer for connections wires */
    private final Pane wireLayer;

    private GhciSession ghci;
    private PreferencesWindow preferences;

    /** The set of blocks that logically belong to this top level */
    private final Set<Block> attachedBlocks;
    
    /**
     * Constructs a new instance.
     */
    public ToplevelPane() {
        super();
        this.attachedBlocks = new HashSet<>();
        
        this.bottomLayer = new Pane();
        this.blockLayer = new Pane(this.bottomLayer);
        this.wireLayer = new Pane(this.blockLayer);
        this.getChildren().add(this.wireLayer);

        this.ghci = new GhciSession();
        this.ghci.startAsync();

        new TouchContext(this);
    }

    public void setPreferences(PreferencesWindow prefs) {
        this.preferences = prefs;
    }

    /** Shows a new function menu at the specified location in this pane. */
    public void showFunctionMenuAt(double x, double y, boolean byMouse) {
        ghci.awaitRunning();
        boolean verticalCurry = this.preferences != null && this.preferences.verticalCurry.isSelected();
        FunctionMenu menu = new FunctionMenu(byMouse, ghci.getCatalog(), this, verticalCurry);
        double verticalCenter = 150; // just a guesstimate, because computing it here is annoying
        menu.relocate(x, y - verticalCenter);
        this.addMenu(menu);
    	
    }
    
    /**
     * @return The Env instance to be used within this CustomUIPane.
     */
    public Environment getEnvInstance() {
        ghci.awaitRunning();
        return ghci.getCatalog().asEnvironment();
    }

    /** Remove the given block from this UI pane, including its connections. */
    public void removeBlock(Block block) {
        block.deleteAllLinks();
        
        if (block.belongsOnBottom()) {
            this.bottomLayer.getChildren().remove(block);
        } else {
            this.blockLayer.getChildren().remove(block);
        }
    }

    /** Attempts to create a copy of a block and add it to this pane. */
    public void copyBlock(Block block) {
        block.getNewCopy().ifPresent(copy -> {
          this.addBlock(copy);
          copy.relocate(block.getLayoutX()+20, block.getLayoutY()+20);
          copy.initiateConnectionChanges();
        });
    }
    
     public GhciSession getGhciSession() {
        return ghci;
    }

    /**
     * Terminate the current GhciSession, if any, then start a new one.
     * Waits for the old session to end, but not for the new session to start.
     */
    public void restartBackend() {
        ghci.stopAsync();
        ghci.awaitTerminated();

        ghci = new GhciSession();
        ghci.startAsync();
    }

    public void addBlock(Block block) {
        if (block.belongsOnBottom()) {
            this.bottomLayer.getChildren().add(block);
        } else {
            this.blockLayer.getChildren().add(block);
        }
    }

    public boolean addMenu(Pane menu) {
        return this.getChildren().add(menu);
    }

    public boolean removeMenu(Pane menu) {
        return this.getChildren().remove(menu);
    }

    public boolean addConnection(Connection connection) {
        return this.wireLayer.getChildren().add(connection);
    }

    public boolean removeConnection(Connection connection) {
        return this.wireLayer.getChildren().remove(connection);
    }

    public boolean addWire(DrawWire drawWire) {
        return this.getChildren().add(drawWire);
    }

    public boolean removeWire(DrawWire drawWire) {
        return this.getChildren().remove(drawWire);
    }

    public boolean addTouchArea(Shape area) {
        return this.getChildren().add(area);
    }
    
    public boolean removeTouchArea(Shape area) {
        return this.getChildren().remove(area);
    }
    
    public void clearChildren() {
        this.bottomLayer.getChildren().clear();
        this.blockLayer.getChildren().remove(1, this.blockLayer.getChildren().size());
        this.wireLayer.getChildren().remove(1, this.blockLayer.getChildren().size());
        this.attachedBlocks.clear();
    }

    public Stream<Node> streamChildren() {
        Stream<Node> bottom = this.bottomLayer.getChildren().stream();
        Stream<Node> blocks = this.blockLayer.getChildren().stream().skip(1);
        Stream<Node> wires  = this.wireLayer.getChildren().stream().skip(1);

        return Stream.concat(bottom, Stream.concat(blocks, wires));
    }

    public Stream<BlockContainer> getAllBlockContainers() {
        return bottomLayer.getChildrenUnmodifiable().stream().flatMap(node ->
            (node instanceof Block) ? ((Block)node).getInternalContainers().stream() : Stream.empty());
    }

    /**
     * Ensures that the ordering of container blocks on the bottom layer is consistent with parent ordering.
     * @param block that might need corrections in the visual ordering. 
     */
    public void moveInFrontOfParentContainers(Block block) {
        if (block.getContainer() instanceof WrappedContainer) {
            Block parent = ((WrappedContainer)block.getContainer()).getWrapper();
            int childIndex = this.bottomLayer.getChildren().indexOf(block);
            int parentIndex = this.bottomLayer.getChildren().indexOf(parent);
            if (childIndex < parentIndex && childIndex >= 0) {
                this.bottomLayer.getChildren().remove(block);
                this.bottomLayer.getChildren().add(parentIndex, block);
                // moving the block after the parent might have caused ordering issues in the block inbetween, resolve them
                for (Node node : new ArrayList<Node>(this.bottomLayer.getChildren().subList(childIndex, parentIndex-1))) {
                    if (node instanceof Block) {
                        this.moveInFrontOfParentContainers((Block)node);
                    }
                }
            }
        }
    }
    
    @Override
    public Bounds getBoundsInScene() {
        return this.localToScene(this.getBoundsInLocal());
    }

    @Override
    public void attachBlock(Block block) {
        this.attachedBlocks.add(block);
    }

    @Override
    public void detachBlock(Block block) {
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

    @Override
    public Node asNode() {
        return this;
    }

    @Override
    public ToplevelPane getToplevel() {
        return this;
    }
}
