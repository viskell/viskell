package nl.utwente.group10.ui.components.blocks;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.ConnectionStateDependent;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
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
public abstract class Block extends StackPane implements ComponentLoader, ConnectionStateDependent {
    /** The pane that is used to hold state and place all components on. */
    private CustomUIPane parentPane;
    
    /** The context menu associated with this block instance. */
    private CircleMenu circleMenu;
    
    /** The connection state this Block is in */
    private int connectionState;
    
    protected Expr signature;
    
    protected Expr expr;
    
    protected BooleanProperty exprDirty;

    /**
     * @param pane
     *            The pane this block belongs to.
     */
    public Block(CustomUIPane pane) {
        parentPane = pane;
        this.exprDirty = new SimpleBooleanProperty(true);

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

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseEvent);

        Platform.runLater(this::createCircleMenu);
    }
    
    public final Boolean getExprDirty() {
        return exprDirty.get();
    }

    public void setExprDirty(Boolean state) {
        this.exprDirty.set(state);
    }
    
    public final BooleanProperty exprDirtyProperty() {
        return exprDirty;
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
        if (parentPane.getSelectedBlock().isPresent()
                && parentPane.getSelectedBlock().get().equals(this)
                && t.getButton().equals(MouseButton.PRIMARY)) {
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
        if (getExprDirty() == false) {
            return expr;
        } else {
            //Should not be necessary.
            updateExpr();
            return expr;
        }
    }
    
    public abstract void updateExpr();

    @Override
    public void invalidateConnectionState() {
    }

    @Override
    public void invalidateConnectionStateCascading(int state) {
        if (!connectionStateIsUpToDate(state)) {
            invalidateConnectionState();
            if (this instanceof OutputBlock) {
                ((OutputBlock) this).getOutputAnchor().invalidateConnectionStateCascading(state);
            }
            this.connectionState = state;
        }
    }
    
    @Override
    public int getConnectionState() {
        return connectionState;
    }
}
