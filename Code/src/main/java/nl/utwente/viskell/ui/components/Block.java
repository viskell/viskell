package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.ui.CircleMenu;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.CustomUIPane;
import nl.utwente.viskell.ui.serialize.Bundleable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private CustomUIPane parentPane;
    
    /** The Circle (Context) menu associated with this block instance. */
    private CircleMenu circleMenu;
    
    /** The local expression of this Block. */
    protected Expression localExpr;
    
    /** Property for the whether the visuals are up to date. */
    protected BooleanProperty staleVisuals;
    
    /** Whether the anchor types are fresh*/
    private boolean freshAnchorTypes;
    
    /** Status of change updating process in this block. */
    private boolean updateInProgress;

    /**
     * @param pane The pane this block belongs to.
     */
    public Block(CustomUIPane pane) {
        this.parentPane = pane;
        this.staleVisuals = new SimpleBooleanProperty(false);
        this.freshAnchorTypes = false;
        this.updateInProgress = false;
        
        this.staleVisuals.addListener(this::fixupVisualState);
        
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
            if (this.circleMenu == null) {
                this.circleMenu = new CircleMenu(this);
            }
            
            this.circleMenu.show(t);
        }
    }

    /** @return the parent CustomUIPane of this component. */
    public final CustomUIPane getPane() {
        return this.parentPane;
    }

    /**
     * @return All InputAnchors of the block.
     */
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of();
    }

    /**
     * @return the optional output Anchor for this Block
     * TODO generalize to List<OutputAnchor> getOutputAnchors()
     */
    public Optional<OutputAnchor> getOutputAnchor() {
        return Optional.empty();
    }
    
    /** @return true if no connected output anchor exist */
    public boolean isBottomMost() {
        return this.getOutputAnchor().map(a -> !a.hasConnection()).orElse(true);
    }
    
    /**
     * Starts a new (2 phase) change propagation process from this block.
     */
    public final void initiateConnectionChanges() {
        this.handleConnectionChanges(false);
        this.handleConnectionChanges(true);
    }
    
    /**
     * Handle the expression and types changes caused by modified connections or values.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    public final void handleConnectionChanges(boolean finalPhase) {
        if (! finalPhase) {
            this.prepareConnectionChanges();
        }
        
        this.propagateConnectionChanges(finalPhase);
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
     * Propagate the changes through connected blocks, then trigger a visual update.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    protected void propagateConnectionChanges(boolean finalPhase) {
        if (this.updateInProgress != finalPhase) {
            return; // avoid doing extra work and infinite recursion
        }
        this.updateInProgress = !finalPhase;
        this.freshAnchorTypes = false;
        
        // First make sure that all connected inputs will be updated too.        
        for (InputAnchor input : this.getAllInputs()) {
            input.getConnection().ifPresent(c -> c.handleConnectionChangesUpwards(finalPhase));
        }
        
        // after type checking all input in the first phase, refresh the local expression of this block
        if (!finalPhase) {
            this.updateExpr();
        }

        // propagate changes down from the output anchor to connected inputs
        this.getOutputAnchor().ifPresent(a -> a.getOppositeAnchors().stream().forEach(x -> x.handleConnectionChanges(finalPhase)));

        // If the change is not propagated any further down start recomputation.
        if (finalPhase && this.isBottomMost()) {
            // Now that the expressions and types are updated, initiate a visual refresh.
            Platform.runLater(() -> this.staleVisuals.set(true));
        }
    }
    
    /**
     * @return The local expression this Block represents.
     */
    public final Expression getLocalExpr() {
        return this.localExpr;
    }
    
    /**
     * @return A complete expression of this block and all its dependencies.
     */
    public final Expression getFullExpr() {
        LetExpression fullExpr = new LetExpression(this.localExpr);
        this.extendExprGraph(fullExpr);
        return fullExpr;
    }

    /**
     * Extends the expression graph to include all subexpression required
     * @param exprGraph the let expression representing the current expression graph
     */
    protected void extendExprGraph(LetExpression exprGraph) {
         for (InputAnchor input : this.getAllInputs()) {
             input.extendExprGraph(exprGraph);
         }
    }
    
    /**
     * Updates the expression
     */
    public abstract void updateExpr();
    
    /**
     * Called when the VisualState changed.
     */
    public abstract void invalidateVisualState();
    
    /**
     * ChangeListener that resolves outdated visuals and 
     * propagates visual update requirements upwards.
     */
    private void fixupVisualState(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newStale) {
        if (newStale) {
            this.invalidateVisualState();
        
            for (InputAnchor input : this.getAllInputs()) {
                input.getOppositeAnchor().ifPresent(a -> a.invalidateVisualState());
            }
            
            this.staleVisuals.setValue(false);
        }
    }

    /**
     * @return class-specific properties of this Block.
     */
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of();
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
