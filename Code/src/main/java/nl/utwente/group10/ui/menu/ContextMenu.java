package nl.utwente.group10.ui.menu;

import nl.utwente.group10.ui.components.blocks.Block;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import jfxtras.scene.layout.CircularPane;
import jfxtras.scene.menu.CirclePopupMenu;

public class ContextMenu extends CirclePopupMenu {

    private Block block;

    public ContextMenu(Block block) {
        super((StackPane) block, null);
        this.block = block;

        // Define menu items
        // TODO implement these options sometime in the future

        // Undo Option
        MenuItem undo = new MenuItem("undo", new ImageView(new Image(this
                .getClass().getResourceAsStream("undo.png"))));

        undo.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                undo();
            }
        });

        // Redo Option
        MenuItem redo = new MenuItem("redo", new ImageView(new Image(this
                .getClass().getResourceAsStream("undo.png"))));

        redo.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                redo();
            }
        });

        // Delete Option
        MenuItem delete = new MenuItem("delete", new ImageView(new Image(this
                .getClass().getResourceAsStream("undo.png"))));

        delete.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                delete();
            }
        });

        // Drawer Option
        MenuItem openDrawer = new MenuItem("openDrawer", new ImageView(
                new Image(this.getClass().getResourceAsStream("undo.png"))));

        openDrawer.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                newFunction();
            }
        });

        // Save Option
        MenuItem save = new MenuItem("save", new ImageView(new Image(this
                .getClass().getResourceAsStream("undo.png"))));

        // Registration
        this.getItems().addAll(undo, redo, delete, openDrawer, save);

        // Animation
        this.setAnimationInterpolation(CircularPane::animateOverTheArcWithFade);
    }

    /** Undo last action. */
    public void undo() {
        // TODO placeholder, will not be implemented
        System.out.println("Clicked Undo!");
    }

    /** Redo last undone action. */
    public void redo() {
        // TODO placeholder, will not be implemented
        System.out.println("Clicked Redo!");
    }

    /** Delete the Block in this context. */
    public void delete() {
        block.getPane().removeBlock(block);
    }

    /** Spawn a new Function drawer. */
    public void newFunction() {
        // TODO spawn function list
        System.out.println("Clicked Drawer!");
    }
}
