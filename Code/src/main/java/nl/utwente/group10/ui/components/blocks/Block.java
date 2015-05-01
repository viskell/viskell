package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.menu.ContextMenu;

/**
 * Base UI Component that other visual elements will extend from. If common
 * functionality is found it should be refactored to here.
 */
public abstract class Block extends StackPane implements ComponentLoader {

    /** The output of this Block. **/
    private OutputAnchor output;
    /** The pane that is used to hold state and place all components on. */
    private CustomUIPane parentPane;
    /** The context menu associated with this block instance. */
    private ContextMenu contextMenu;

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

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, this::select);

        this.createContextMenu();
    }

    private void createContextMenu() {
        contextMenu = new ContextMenu(this);
    }

    /** Returns the output Anchor for this Block. */
    public final OutputAnchor getOutputAnchor() {
        return output;
    }

    /**
     * Sets this block as the selected block. When this block has already been
     * selected it spawns a contextMenu instead.
     */
    private void select(MouseEvent mouseEvent) {
        if (parentPane.getSelectedBlock().isPresent()
                && parentPane.getSelectedBlock().get().equals(this)) {
            contextMenu.show(mouseEvent);
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
