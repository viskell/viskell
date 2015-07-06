package nl.utwente.group10.ui.components.blocks;

import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.ConnectionStateDependent;
import nl.utwente.group10.ui.components.CustomAlert;
import nl.utwente.group10.ui.components.VisualStateDependent;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.blocks.input.InputBlock;
import nl.utwente.group10.ui.components.blocks.output.OutputBlock;
import nl.utwente.group10.ui.exceptions.TypeUnavailableException;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;
import nl.utwente.group10.ui.components.menu.CircleMenu;

/**
 * Base block shaped UI Component that other visual elements will extend from.
 * Blocks can add input and output values by implementing the InputBlock and
 * OutputBlock interfaces. Blocks typically are dependent on the ConnectionState
 * and VisualState, although the default invalidateConnectionState() and
 * invalidateVisualState() implementation do nothing.
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
public abstract class Block extends StackPane implements ComponentLoader, ConnectionStateDependent, VisualStateDependent {
    /** The pane that is used to hold state and place all components on. */
    private CustomUIPane parentPane;
    
    /** The Circle (Context) menu associated with this block instance. */
    private CircleMenu circleMenu;
    
    /** The expression of this Block. */
    protected Expr expr;
    
    /** Property for the ConnectionState. */
    protected IntegerProperty connectionState;
    
    /** Property for the VisualState. */
    protected IntegerProperty visualState;
    
    /** Property for the expression freshness. */
    private BooleanProperty exprIsDirty;

    /**
     * @param pane
     *            The pane this block belongs to.
     */
    public Block(CustomUIPane pane) {
        parentPane = pane;
        connectionState = new SimpleIntegerProperty(ConnectionCreationManager.getConnectionState());
        visualState = new SimpleIntegerProperty(ConnectionCreationManager.getConnectionState());
        exprIsDirty = new SimpleBooleanProperty(true);
        
        // Add listeners to the states.
        // Invalidate listeners give the Block a change to react on the state
        // change before it is cascaded.
        // Cascade listeners make sure state changes are correctly propagated.
        connectionState.addListener(a -> invalidateConnectionState());
        connectionState.addListener(a -> setExprIsDirty(true));
        connectionState.addListener(this::cascadeConnectionState);
        visualState.addListener(a -> invalidateVisualState());
        visualState.addListener(this::cascadeVisualState);
        
        // Visually react on selection.
        parentPane.selectedBlockProperty().addListener(event -> {
            if (parentPane.getSelectedBlock().isPresent() && this.equals(parentPane.getSelectedBlock().get())) {
                this.getStyleClass().add("selected");
            } else {
                this.getStyleClass().removeAll("selected");
            }
        });
        // Add selection trigger.
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseEvent);

        // Create the CircleMenu after fully creating the Block.
        Platform.runLater(this::createCircleMenu);
    }

    /**
     * Creates the CircleMenu for this Block.
     */
    protected void createCircleMenu() {
        circleMenu = new CircleMenu(this);
    }

    /**
     * Sets this block as the selected block.
     * A right click also opens the CircleMenu.
     */
    private void handleMouseEvent(MouseEvent t) {
        parentPane.setSelectedBlock(this);
        if (t.getButton() == MouseButton.SECONDARY) {
            circleMenu.show(t);
        }
    }

    /** @return the parent CustomUIPane of this component. */
    public final CustomUIPane getPane() {
        return parentPane;
    }

    /**
     * @return The expression this Block represents.
     * 
     * If the expression is not up-to-date it gets updated.
     */
    public Expr getExpr() {
        // Assure expr is up-to-date.
        if (getExprIsDirty()) {
            updateExpr();
        }
        return expr;
    }
    
    /**
     * Updates the expression, clearing the expression dirty flag.
     */
    public void updateExpr() {
        setExprIsDirty(false);
    }
    
    @Override
    public final int getVisualState() {
        return visualState.get();
    }

    @Override
    public void setVisualState(int state) {
        this.visualState.set(state);
    }
    
    @Override
    public final IntegerProperty visualStateProperty() {
        return visualState;
    }
    
    @Override
    public int getConnectionState() {
        return connectionState.get();
    }

    @Override
    public void setConnectionState(int state) {
        this.connectionState.set(state);
    }
    
    @Override
    public final IntegerProperty connectionStateProperty() {
        return connectionState;
    }
    
    /**
     * @return True if the expression is not fresh.
     */
    public boolean getExprIsDirty() {
        return exprIsDirty.get();
    }
    
    /**
     * Sets the dirty-ness of the expression
     */
    public void setExprIsDirty(boolean dirty) {
        exprIsDirty.set(dirty);
    }
    
    /**
     * @return The BooleanProperty for the expression dirty-ness.
     */
    public BooleanProperty exprIsDirtyProperty() {
        return exprIsDirty;
    }
    
    /**
     * Called when the ConnectionState changed.
     */
    public void invalidateConnectionState() {
        // Default does nothing.
    }
    
    /**
     * Called when the VisualState changed.
     */
    public void invalidateVisualState() {
        // Default does nothing.
    }
    
    /**
     * ChangeListener that propagates the new ConnectionState to other Blocks
     * that use this Block's output as input.
     * 
     * When the ConnectionState can not be propagated further, a VisualState
     * cascade gets triggered in the reverse direction.
     */
    public void cascadeConnectionState(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (oldValue != newValue) {
            // Boolean to check if this was the last Block that changed.
            boolean cascadedFurther = false;
            if (this instanceof OutputBlock) {
                OutputBlock oblock = (OutputBlock) this;
                for (Optional<? extends ConnectionAnchor> anchor : oblock.getOutputAnchor().getOppositeAnchors()) {
                    if (anchor.isPresent()) {
                        // This Block is an OutputBlock, and that Output is connected to at least 1 Block.
                        anchor.get().getBlock().setConnectionState(newValue.intValue());
                        cascadedFurther = true;
                    }
                }
            }
            
            
            if (!cascadedFurther) {
                // The ConnectionState change is not cascaded any further, now a
                // visual update should be propagated upwards.
                try {
                    // Analyze the entire tree.
                    this.getExpr().analyze(getPane().getEnvInstance());
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
                    if (e.getHaskellObject() instanceof Expr) {
                        Expr errorExpr = (Expr) e.getHaskellObject();
                        while (errorExpr instanceof Apply) {
                            errorExpr = ((Apply) errorExpr).getChildren().get(0);
                            index++;
                        }
                        // Get the Block in which the type error occurred and
                        // set the error state for the mismatched input to true.
                        getPane().getExprToFunction(errorExpr).getInput(index).setErrorState(true);
                        // Indicate that an error occurred in the latest analyze attempt.
                        getPane().setErrorOccurred(true);
                    } else {
                        // Without an expression we do not really now where the
                        // type error occurred, so ignore it and let GHCi handle
                        // it.
                        informUnkownHaskellException();
                        throw new TypeUnavailableException();
                    }
                } catch (HaskellException e) {
                    // This should be impossible:
                    // The possible exception would come from an Ident not found in the catalog,
                    // A FunctionBlock without a valid Ident should never exist.
                    informUnkownHaskellException();
                    throw new TypeUnavailableException();
                }
                
                // Now that the expressions are updated, propagate a visual refresh upwards.
                this.setVisualState((int) newValue);
                
            }
        }
    }
    
    /**
     * ChangeListener that propagates the new VisualState to other Blocks
     * used as input for this Block.
     */
    public void cascadeVisualState(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (this instanceof InputBlock) {
            InputBlock iblock = (InputBlock) this;
            for (InputAnchor input : iblock.getActiveInputs()) {
                if (input.isPrimaryConnected()) {
                    input.getPrimaryOppositeAnchor().get().getBlock().setVisualState((int) newValue);
                }
            }
        }
    }
    
    protected void informUnkownHaskellException() {
        String msg = "Whoops! An unkown error has occured. We're sorry, but can't really tell you more than this.";
        CustomAlert alert = new CustomAlert(getPane(), msg);
        getPane().getChildren().add(alert);
        alert.relocate(this.getLayoutX() + 100, this.getLayoutY() + 100);

    }
}
