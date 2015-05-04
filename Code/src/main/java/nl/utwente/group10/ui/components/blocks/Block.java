package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.menu.CircleMenu;

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

    /** The output of this Block. **/
    private OutputAnchor output;
    /** The pane that is used to hold state and place all components on. */
    private CustomUIPane parentPane;
    /** The context menu associated with this block instance. */
    private CircleMenu contextMenu;

    /**
     * @param blockName
     *            Name of this block. The name is used to load the FXML
     *            definition for this block.
     * @param pane
     *            The pane this block belongs to.
     */
    public Block(CustomUIPane pane) {
        parentPane = pane;

        try {
            output = new OutputAnchor(this, pane);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        this.createContextMenu();
    }

    private void createContextMenu() {
        contextMenu = new CircleMenu(this);
    }

    /** Returns the output Anchor for this Block. */
    public final OutputAnchor getOutputAnchor() {
        return output;
    }

    /**
     * Sets this block as the selected block. When this block has already been
     * selected it spawns a contextMenu instead.
     */
    private void handleMouseEvent(MouseEvent t) {
        if (parentPane.getSelectedBlock().isPresent()
                && parentPane.getSelectedBlock().get().equals(this)
                && t.getButton().equals(MouseButton.PRIMARY)) {
            contextMenu.show(t);
        } else {
            parentPane.setSelectedBlock(this);
        }
    }

    /** Returns the parent pane of this Component. */
    public final CustomUIPane getPane() {
        return parentPane;
    }

    /** Returns an expression that evaluates to what this block is. */
    public abstract Expr asExpr();

    /** DEBUG METHOD trigger the error state for this Block */
    public abstract void error();
}
