package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.layout.StackPane;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.CircleMenu;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.CustomUIPane;
import nl.utwente.viskell.ui.DragContext;
import nl.utwente.viskell.ui.TrashContainer;
import nl.utwente.viskell.ui.serialize.Bundleable;

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
    protected BlockContainer container;

    /**
     * @param pane The pane this block belongs to.
     */
    public Block(CustomUIPane pane) {
        this.parentPane = pane;
        this.freshAnchorTypes = false;
        this.updateInProgress = false;
        this.container = pane.getTopLevel();
        this.container.attachBlock(this);
        
        this.dragContext = new DragContext(this);
        this.dragContext.setSecondaryClickAction(p -> CircleMenu.showFor(this, this.localToScreen(p)));
        dragContext.setDragFinishAction(event -> refreshContainer());
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
        
        // propagate changes to the outside of a choiceblock
        if (container instanceof Lane) {
            ((Lane)container).handleConnectionChanges(finalPhase);
        }
        
        if (finalPhase) {
            // Now that the expressions and types are fully updated, initiate a visual refresh.
            Platform.runLater(() -> this.invalidateVisualState());
        }
    }
    
    /** @return A pair containing the expression this block represents and a set of out-of-reach OutputAnchors. */
    public abstract Pair<Expression,Set<OutputAnchor>> getLocalExpr();
    
    /**
     * @return A complete expression of this block and all its dependencies.
     */
    public final Expression getFullExpr() {
        Pair<Expression, Set<OutputAnchor>> localExpr = getLocalExpr();
        
        LetExpression fullExpr = new LetExpression(localExpr.a, false);
        Set<OutputAnchor> outerAnchors = localExpr.b;
        extendExprGraph(fullExpr, this.parentPane.getTopLevel(), outerAnchors);
        
        outerAnchors.forEach(block -> block.extendExprGraph(fullExpr, this.parentPane.getTopLevel(), new HashSet<>()));
        
        return fullExpr;
    }

    /**
     * Extends the expression graph to include all subexpression required
     * @param exprGraph the let expression representing the current expression graph
     * @param container the container to which this expression graph is constrained
     * @param outsideAnchors a mutable set of required OutputAnchors from a surrounding container
     */
    protected void extendExprGraph(LetExpression exprGraph, BlockContainer container, Set<OutputAnchor> outsideAnchors) {
         for (InputAnchor input : this.getAllInputs()) {
             input.extendExprGraph(exprGraph, container, outsideAnchors);
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
    
    /** @return the container to which this block belongs */
    public BlockContainer getContainer() {
        return container;
    }

    /** @return an independent copy of this Block, or Optional.empty if the internal state is too complex too copy.  */
    public abstract Optional<Block> getNewCopy();
    
    /** Remove all associations of this block with others in preparation of removal, including all connections */
    public void deleteAllLinks() {
        this.getAllInputs().forEach(InputAnchor::removeConnections);
        this.getAllOutputs().forEach(OutputAnchor::removeConnections);
        this.container.removeBlock(this);
        this.container = TrashContainer.instance;
    }
    
    public void moveIntoContainer(BlockContainer target) {
        BlockContainer source = this.container;
        if (source != target) {
            this.container.removeBlock(this);
            this.container = target;
            target.attachBlock(this);
            
            if (source instanceof WrappedContainer) {
                ((WrappedContainer)source).handleConnectionChanges(false);
                ((WrappedContainer)source).handleConnectionChanges(true);
            }
            
            if (target instanceof WrappedContainer) {
                ((WrappedContainer)target).handleConnectionChanges(false);
                ((WrappedContainer)target).handleConnectionChanges(true);
            }
            
            this.initiateConnectionChanges();
        }
    }
    
    /** Scans for and attaches to a new container, if any */
    public void refreshContainer() {
        Bounds myBounds = localToScene(getBoundsInLocal());
        Point2D center = new Point2D((myBounds.getMinX()+myBounds.getMaxX())/2, (myBounds.getMinY()+myBounds.getMaxY())/2);
        List<Point2D> corners = ImmutableList.of(
                new Point2D(myBounds.getMinX(), myBounds.getMinY()),
                new Point2D(myBounds.getMaxX(), myBounds.getMinY()),
                new Point2D(myBounds.getMinX(), myBounds.getMaxY()),
                new Point2D(myBounds.getMaxX(), myBounds.getMaxY()));
        
        // use center point plus one corner to determine wherein this block is, to ease moving a block into a small container
        Predicate<Bounds> within = bounds -> bounds.contains(center) && corners.stream().anyMatch(p -> bounds.contains(p));
        
        BlockContainer newContainer = parentPane.getBlockContainers().
            filter(container -> within.test(container.getBoundsInScene())).
                reduce((a, b) -> !a.getBoundsInScene().contains(b.getBoundsInScene()) ? a : b).
                    orElse(this.parentPane.getTopLevel());
        
        this.moveIntoContainer(newContainer);
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
