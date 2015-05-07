package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;

/**
 * Base block shaped UI Component that other visual elements will extend from.
 * All components that can represent their information in a block always have at
 * least an output anchor. Input anchors are optionally implemented.
 * 
 * Blocks should all have a way to indicate an error state that is relevant to
 * their implementation.
 * 
 * MouseEvents are used for setting the selection state of a block, single
 * clicks can toggle the state to selected. When a block has already been
 * selected and receives another single left click it will toggle a contextual
 * menu for the block.
 * 
 * Each block implementation should also feature it's own FXML implementation.
 */
public abstract class Block extends StackPane implements ComponentLoader {

    /** The pane that is used to hold state and place all components on. */
    private CustomUIPane parentPane;

    /**
     * @param blockName
     *            Name of this block. The name is used to load the FXML
     *            definition for this block.
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

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, this::select);
    }

    /** Sets this block as the selected block. */
    private void select(MouseEvent mouseEvent) {
        parentPane.setSelectedBlock(this);
    }

    /** Returns the parent pane of this Component. */
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
     */
    public void invalidate() {
    }

    /** DEBUG METHOD trigger the error state for this Block */
    public abstract void error();
}
