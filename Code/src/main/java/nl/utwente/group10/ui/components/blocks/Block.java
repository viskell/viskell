package nl.utwente.group10.ui.components.blocks;

import javafx.application.Platform;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;
import nl.utwente.group10.ui.menu.CircleMenu;

/**
 * Base block shaped UI Component that other visual elements will extend from.
 * All components that can represent their information in a block always have at
 * least an {@link OutputAnchor}. {@link InputAnchor}s are optionally implemented.
 * <p>
 * Blocks should all have a way to indicate an error state that is relevant to
 * their implementation.
 * </p>
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
public abstract class Block extends StackPane implements ComponentLoader {

    /** The pane that is used to hold state and place all components on. */
    private CustomUIPane parentPane;
    /** The context menu associated with this block instance. */
    private CircleMenu circleMenu;
    
    /** The connection state this Block is in */
    private int connectionState;

    /**
     * @param pane
     *            The pane this block belongs to.
     */
    public Block(CustomUIPane pane) {
        parentPane = pane;

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

    protected void createCircleMenu() {
        circleMenu = new CircleMenu(this);
    }

    /**
     * Sets this block as the selected block. When this block has already been
     * selected it spawns a {@link CircleMenu} instead.
     */
    private void handleMouseEvent(MouseEvent t) {
        /*
        if (parentPane.getSelectedBlock().isPresent()
                && parentPane.getSelectedBlock().get().equals(this)
                && t.getButton().equals(MouseButton.PRIMARY)) {
            circleMenu.show(t);
        } else {
            parentPane.setSelectedBlock(this);
        }
        */
    }

    /** Returns the parent pane of this component. */
    public final CustomUIPane getPane() {
        return parentPane;
    }

    /** Returns an expression that evaluates to what this block is. */
    public abstract Expr asExpr();

    /**
     * Tells the block that its current state (considering connections) possibly has
     * changed. Default implementation does not react to a potential state
     * change.
     *
     * This method should only be called after the Block's constructor is done.
     * 
     * This method will invalidate the Block even if the state did not change.
     */
    public void invalidateConnectionState() {
    }

    /**
     * Does the same as invalidateConnectionState(), but cascading down to other
     * blocks which are possibly also (indirectly) affected by the state change.
     * 
     * Cascading only happens if this Block is not up-to-date, implying that if
     * this Block is up-to-date, then so are all following Blocks.
     * 
     * @param state
     *            The newest visual state
     */
    public void invalidateConnectionStateCascading(int state) {
        if (!connectionStateIsUpToDate(state)) {
            invalidateConnectionState();
            if (this instanceof OutputBlock) {
                ((OutputBlock) this).getOutputAnchor().invalidateConnectionStateCascading(state);
            }
            this.connectionState = state;
        }
    }

    /**
     * Shortcut to call invalidateConnectionStateCascading(int state) with the newest state.
     */
    public void invalidateConnectionStateCascading() {
        invalidateConnectionStateCascading(ConnectionCreationManager.getConnectionState());
    }

    /**
     * @return Whether or not the state of the block confirms to the given newest state.
     */
    public boolean connectionStateIsUpToDate(int state) {
        return this.connectionState == state;
    }

    /**
     * Enables or disables the error state of this Block.
     * @param error True to enable, False to disable.
     */
    public void setError(boolean error) {
        //TODO is this method useful?
        if (error) {
            this.getStyleClass().removeAll("error");
            this.getStyleClass().add("error");
        } else {
            this.getStyleClass().removeAll("error");
        }
    }
}
