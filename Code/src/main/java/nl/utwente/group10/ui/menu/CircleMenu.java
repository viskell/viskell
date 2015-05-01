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

/**
 * Circle menu is a context based menu implementation for block classes.
 * Preferably each block class has it's own instance if a circle menu. When a
 * block based class has significant differences to other block classes that
 * result in different context based actions it should use an specialized
 * extension of circle menu instead of this one.
 * 
 * Current context based features include delete. During development undo, redo
 * and newFunction features will be listed here.
 */
public class CircleMenu extends CirclePopupMenu {

    private Block block;

    public CircleMenu(Block block) {
        super((StackPane) block, null);
        this.block = block;

        // TODO implement undo and redo if time permits.
        // TODO consider copy/cut/paste as context actions?

        // Define menu items

        // Undo Option
        MenuItem undo = new MenuItem("undo", new ImageView(new Image(this
                .getClass().getResourceAsStream("/ui/undo.png"))));
        undo.setOnAction(t -> undo());

        // Redo Option
        MenuItem redo = new MenuItem("redo", new ImageView(new Image(this
                .getClass().getResourceAsStream("/ui/undo.png"))));
        redo.setOnAction(t -> redo());

        // Delete Option
        MenuItem delete = new MenuItem("delete", new ImageView(new Image(this
                .getClass().getResourceAsStream("/ui/undo.png"))));
        delete.setOnAction(t -> delete());

        // Drawer Option
        MenuItem openDrawer = new MenuItem("openDrawer", new ImageView(
                new Image(this.getClass().getResourceAsStream("/ui/undo.png"))));
        openDrawer.setOnAction(t -> newFunction());

        // Save Option
        MenuItem save = new MenuItem("save", new ImageView(new Image(this
                .getClass().getResourceAsStream("/ui/undo.png"))));

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
