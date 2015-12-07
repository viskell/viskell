package nl.utwente.viskell.ui.components;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.ui.CircleMenu;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.CustomUIPane;
import nl.utwente.viskell.ui.DragContext;
import nl.utwente.viskell.ui.serialize.Bundleable;

import com.google.common.collect.ImmutableMap;

/**
 * Base block shaped UI Component that other visual elements will extend from.
 * Blocks can add input and output values by implementing the InputBlock and
 * OutputBlock interfaces.
 * <p>
 * MouseEvents are used for setting the selection state of a block, single
 * clicks can toggle the state to selected. When a block has already been
 * selected and receives another single left click it will toggle a contextual
 * menu for the block.
 * </p>
 * <p>
 * Each block implementation should also feature it's own FXML implementation.
 * </p>
 */
public abstract class Block extends StackPane implements Bundleable, ComponentLoader {
    /** The pane that is used to hold state and place all components on. */
    private final CustomUIPane parentPane;
    
    /** The context that deals with dragging and touch event for this Block */
    protected DragContext dragContext;
    
    /** Whether the anchor types are fresh*/
    private boolean freshAnchorTypes;
    
    /** Status of change updating process in this block. */
    private boolean updateInProgress;
    
    /** The container to which this Block currently belongs */
    protected Optional<BlockContainer> container;

    /**
     * @param pane The pane this block belongs to.
     */
    public Block(CustomUIPane pane) {
        this.parentPane = pane;
        this.freshAnchorTypes = false;
        this.updateInProgress = false;
        this.dragContext = new DragContext(this);
        this.container = Optional.empty();
        
        dragContext.setDragInitAction(event -> detachFromContainer());
        dragContext.setDragFinishAction(event -> refreshContainer());
        
        // Visually react on selection.
        this.parentPane.selectedBlockProperty().addListener(event -> {
            if (parentPane.getSelectedBlock().isPresent() && this.equals(parentPane.getSelectedBlock().get())) {
                this.getStyleClass().add("selected");
            } else {
                this.getStyleClass().removeAll("selected");
            }
        });
        // Add selection trigger.
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseEvent);
    }

    /**
     * Sets this block as the selected block.
     * A right click also opens the CircleMenu.
     */
    private void handleMouseEvent(MouseEvent t) {
        parentPane.setSelectedBlock(this);
        if (t.getButton() == MouseButton.SECONDARY) {
            CircleMenu.showFor(this, t);
        }
    }

    /** @return the parent CustomUIPane of this component. */
    public final CustomUIPane getPane() {
        return this.parentPane;
    }

    /**
     * @return All InputAnchors of the block.
     */
    public abstract List<InputAnchor> getAllInputs();

    /**
     * @return All OutputAnchors of the Block.
     */
    public abstract List<OutputAnchor> getAllOutputs();
    
    /** @return true if no connected output anchor exist */
    public boolean isBottomMost() {
        for (OutputAnchor anchor : getAllOutputs()) {
            if (anchor.hasConnection()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Starts a new (2 phase) change propagation process from this block.
     */
    public final void initiateConnectionChanges() {
        this.handleConnectionChanges(false);
        this.handleConnectionChanges(true);
    }
    
    /**
     * Connection change preparation; set fresh types in all anchors. 
     */
    public final void prepareConnectionChanges() {
        if (this.updateInProgress || this.freshAnchorTypes) {
            return; // refresh anchor types in each block only once
        }
        this.freshAnchorTypes = true;
        this.refreshAnchorTypes();
    }
    
    /**
     * Set fresh types in all anchors of this block for the next typechecking cycle.
     */
    protected abstract void refreshAnchorTypes();
    
    /**
     * Handle the expression and types changes caused by modified connections or values.
     * Propagate the changes through connected blocks, and if final phase trigger a visual update.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    public void handleConnectionChanges(boolean finalPhase) {
        if (this.updateInProgress != finalPhase) {
            return; // avoid doing extra work and infinite recursion
        }

        if (! finalPhase) {
            // in first phase ensure that anchor types are refreshed
            this.prepareConnectionChanges();
        }
        
        this.updateInProgress = !finalPhase;
        this.freshAnchorTypes = false;
        
        // First make sure that all connected inputs will be updated too.        
        for (InputAnchor input : this.getAllInputs()) {
            input.getConnection().ifPresent(c -> c.handleConnectionChangesUpwards(finalPhase));
        }
        
        // propagate changes down from the output anchor to connected inputs
        this.getAllOutputs().stream().forEach(output -> output.getOppositeAnchors().forEach(input -> input.handleConnectionChanges(finalPhase)));
        
        if (finalPhase) {
            // Now that the expressions and types are fully updated, initiate a visual refresh.
            Platform.runLater(() -> this.invalidateVisualState());
        }
    }
    
    /**
     * @return The local expression this Block represents.
     */
    public abstract Pair<Expression,Set<Block>> getLocalExpr();
    
    /**
     * @return A complete expression of this block and all its dependencies.
     */
    public final Expression getFullExpr() {
        Pair<Expression, Set<Block>> localExpr = getLocalExpr();
        
        LetExpression fullExpr = new LetExpression(localExpr.a, false);
        Set<Block> surroundingBlocks = localExpr.b;
        extendExprGraph(fullExpr, null, surroundingBlocks);
        
        surroundingBlocks.forEach(block -> block.extendExprGraph(fullExpr, null, new HashSet<>()));
        
        return fullExpr;
    }

    /**
     * Extends the expression graph to include all subexpression required
     * @param exprGraph the let expression representing the current expression graph
     * @param container the container to which this expression graph is constrained
     * @param addLater a mutable list of blocks that have to be added by a surrounding container
     */
    protected void extendExprGraph(LetExpression exprGraph, BlockContainer container, Set<Block> addLater) {
         for (InputAnchor input : this.getAllInputs()) {
             input.extendExprGraph(exprGraph, container, addLater);
         }
    }
    
    /** Called when the VisualState changed. */
    public abstract void invalidateVisualState();

    /** @return whether this block is visually shown below common blocks (is constant per instance). */
    public boolean belongsOnBottom() {
        return false;
    }
    
    /** @return class-specific properties of this Block. */
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of();
    }
    
    /** @return the container to which this block belongs, if any */
    public Optional<BlockContainer> getContainer() {
        return container;
    }
    
    /** Removes the block from its container */
    public void detachFromContainer() {
        container.ifPresent(container -> {
            this.container = Optional.empty();
            container.detachBlock(this);
        });
    }

    /** Scans for and attaches to a new container, if any */
    public void refreshContainer() {
        Bounds myBounds = localToScene(getBoundsInLocal());
        detachFromContainer();
        
        container = parentPane.getBlockContainers().
            filter(container -> container.localToScene(container.getBoundsInLocal()).contains(myBounds)).
                reduce((a, b) -> !a.localToScene(a.getBoundsInLocal()).contains(b.localToScene(b.getBoundsInLocal())) ? a : b);
        
        container.ifPresent(container -> container.attachBlock(this));
    }
    
    @Override
    public Map<String, Object> toBundle() {
        return ImmutableMap.of(
            "kind", getClass().getSimpleName(),
            "id", hashCode(),
            "x", getLayoutX(),
            "y", getLayoutY(),
            "properties", toBundleFragment()
        );
    }
}
