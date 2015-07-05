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
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.ConnectionStateDependent;
import nl.utwente.group10.ui.components.VisualStateDependent;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.function.FunctionBlock;
import nl.utwente.group10.ui.components.blocks.input.InputBlock;
import nl.utwente.group10.ui.components.blocks.output.OutputBlock;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;
import nl.utwente.group10.ui.menu.CircleMenu;

/**
 * Base block shaped UI Component that other visual elements will extend from.
 * Blocks can add input and output values by implementing the InputBlock and
 * OutputBlock interfaces. Blocks typically are dependent on the
 * ConnectionState, although the default invalidateConnectionState()
 * implementation does nothing.
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
    
    /** The context menu associated with this block instance. */
    private CircleMenu circleMenu;
    
    protected Expr signature;
    
    protected Expr expr;
    
    protected IntegerProperty connectionState;
    protected IntegerProperty visualState;
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
        
        connectionState.addListener(a -> invalidateConnectionState());
        connectionState.addListener(a -> setExprIsDirty(true));
        connectionState.addListener(this::cascadeConnectionState);
        visualState.addListener(a -> invalidateVisualState());
        visualState.addListener(this::cascadeVisualState);
        
        parentPane.selectedBlockProperty()
                .addListener(
                        event -> {
                            if (parentPane.getSelectedBlock().isPresent()
                                    && this.equals(parentPane
                                            .getSelectedBlock().get())) {
                                this.getStyleClass().add("selected");
                            } else {
                                this.getStyleClass().removeAll("selected");
                            }
                        });
        this.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseEvent);

        Platform.runLater(this::createCircleMenu);
    }

    /**
     * Creates the CircleMenu for this Block.
     */
    protected void createCircleMenu() {
        circleMenu = new CircleMenu(this);
    }

    /**
     * Sets this block as the selected block. When this block has already been
     * selected it spawns a {@link CircleMenu} instead.
     */
    private void handleMouseEvent(MouseEvent t) {
        if (t.getButton() == MouseButton.SECONDARY) {
            circleMenu.show(t);
        } else {
            parentPane.setSelectedBlock(this);
        }
    }

    /** Returns the parent pane of this component. */
    public final CustomUIPane getPane() {
        return parentPane;
    }

    /** Returns an expression that evaluates to what this block is. */
    public Expr getExpr() {
        // Assure expr is up-to-date.
        if (getExprIsDirty()) {
            updateExpr();
        }
        return expr;
    }
    
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
    
    public boolean getExprIsDirty() {
        return exprIsDirty.get();
    }
    
    public void setExprIsDirty(boolean dirty) {
        exprIsDirty.set(dirty);
    }
    
    public BooleanProperty exprIsDirtyProperty() {
        return exprIsDirty;
    }
    
    public void invalidateConnectionState() {
        // Default does nothing.
    }
    
    public void invalidateVisualState() {
        // Default does nothing.
    }
    
    public void cascadeConnectionState(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (oldValue != newValue) {
            boolean cascadedFurther = false;
            if (this instanceof OutputBlock) {
                OutputBlock oblock = (OutputBlock) this;
                for (Optional<? extends ConnectionAnchor> anchor : oblock.getOutputAnchor().getOppositeAnchors()) {
                    if (anchor.isPresent()) {
                        anchor.get().getBlock().setConnectionState((int) newValue);
                        cascadedFurther = true;
                    }
                }
            }
            
            if (!cascadedFurther) {
                try {
                    // Analyze the entire tree.
                    this.getExpr().analyze(getPane().getEnvInstance());
                    getPane().setErrorOccurred(false);
                } catch (HaskellTypeError e) {
                    // A Type mismatch occurred.
                    int index = -1;
                    if (e.getHaskellObject() instanceof Expr) {
                        Expr errorExpr = (Expr) e.getHaskellObject();
                        while (errorExpr instanceof Apply) {
                            errorExpr = ((Apply) errorExpr).getChildren().get(0);
                            index++;
                        }
                        getPane().getExprToFunction(errorExpr).getInput(index).setErrorState(true);
                        getPane().setErrorOccurred(true);
                    } else {
                        // TODO Now what?
                    }
                } catch (HaskellException e) {
                    // TODO Now what?
                    e.printStackTrace();
                }
                // Now that the expressions are updated, propagate a visual refresh upwards.
                this.setVisualState((int) newValue);
            }
        }
    }
    
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
}
