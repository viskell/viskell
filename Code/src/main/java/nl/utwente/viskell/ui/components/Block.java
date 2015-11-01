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
import nl.utwente.viskell.haskell.expr.Apply;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.ui.CircleMenu;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.CustomAlert;
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
    
    /** The expression of this Block. */
    protected Expression expr;
    
    /** Property for the whether the visuals are up to date. */
    protected BooleanProperty staleVisuals;
    
    /** Marker for the expression freshness. */
    protected boolean exprIsDirty;

    /**
     * @param pane The pane this block belongs to.
     */
    public Block(CustomUIPane pane) {
        this.parentPane = pane;
        this.staleVisuals = new SimpleBooleanProperty(false);
        this.exprIsDirty = false;
        
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
    
    /**
     * @return The expression this Block represents.
     * 
     * If the expression is not up-to-date it gets updated.
     */
    public final Expression getExpr() {
        // Assure expr is up-to-date.
        if (this.exprIsDirty) {
            this.updateExpr();
            this.exprIsDirty = false;
        }
        return expr;
    }
    
    /**
     * Updates the expression
     */
    public abstract void updateExpr();
    
    /**
     * Set fresh types in all anchors of this block for the next typechecking cycle.
     */
    public abstract void refreshAnchorTypes();
    
    /**
     * Called when the VisualState changed.
     */
    public abstract void invalidateVisualState();
    
    /**
     * Handle the expression and types changes caused by modified connections or values.
     * After propagating the changes through connected blocks, a visual update is triggered.
     */
    public void handleConnectionChanges() {
        if (this.exprIsDirty) {
            return; // avoid doing extra work and infinite recursion
        }
        
        // Set the expression to dirty
        this.exprIsDirty = true;

        // Set fresh types in all anchors for the next typechecking cycle.
        this.refreshAnchorTypes();
        
        // First make sure that all connected inputs will be updated too.        
        for (InputAnchor input : this.getAllInputs()) {
            input.getOppositeAnchor().ifPresent(a -> a.handleConnectionChanges());
        }

        // Boolean to check if this was the last Block that changed.
        boolean propagatedDown = false;
        if (this.getOutputAnchor().isPresent()) {
            for (Optional<InputAnchor> anchor : this.getOutputAnchor().get().getOppositeAnchors()) {
                if (anchor.isPresent()) {
                    anchor.get().handleConnectionChanges();
                    propagatedDown = true;
                }
            }
        }

        // Now the change is not propagated any further start recomputation.
        if (!propagatedDown) {
            // Needs to be delayed, because recomputation clears exprIsDirty also used to avoid infinite recursion.
            Platform.runLater(this::recomputeExpression);
        }
    }
    
    /** Reconstruct dirty expressions and typechecks them. */
    private void recomputeExpression() {
        if (!this.exprIsDirty) {
            return; // do not recompute more than once.
        }
        
        try {
            // Analyze the entire tree.
            this.getExpr().findType();
            getPane().setErrorOccurred(false);
            // TODO: This will set the errorOccurred for the entire
            // program, not just the invalidated tree. This means that
            // when having multiple small program trees, errors get
            // reset to quickly.

            // No type mismatches.
        } catch (HaskellTypeError e) {
            // A Type mismatch occurred.
            int index = -1;
            // Determine the input index of the Type error.
            Expression errorExpr = e.getExpression();
            while (errorExpr instanceof Apply) {
                errorExpr = ((Apply) errorExpr).getChildren().get(0);
                index++;
            }
            // Get the Block in which the type error occurred and
            // set the error state for the mismatched input to true.
            getPane().getExprToFunction(errorExpr).getInput(index).setErrorState(true);
            // Indicate that an error occurred in the latest analyze attempt.
            getPane().setErrorOccurred(true);
        }

        // Now that the expressions and types are updated, initiate a visual refresh.
        this.staleVisuals.set(true);
    }
    
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
    
    protected void informUnkownHaskellException() {
        String msg = "Whoops! An unkown error has occured. We're sorry, but can't really tell you more than this.";
        CustomAlert alert = new CustomAlert(getPane(), msg);
        getPane().getChildren().add(alert);
        alert.relocate(this.getLayoutX() + 100, this.getLayoutY() + 100);
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
