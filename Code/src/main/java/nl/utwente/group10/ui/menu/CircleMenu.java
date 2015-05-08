package nl.utwente.group10.ui.menu;

import nl.utwente.group10.ui.components.blocks.Block;

import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import jfxtras.scene.layout.CircularPane;
import jfxtras.scene.menu.CirclePopupMenu;

/**
 * Circle menu is a context based menu implementation for {@link Block} classes.
 * Preferably each block class has it's own instance of circle menu. When a
 * block based class has significant differences to other block classes that
 * result in different context based actions it should use a specialized
 * extension of circle menu instead of this one.
 * <p>
 * Current context based features include delete. Copy, Paste and Save
 * functionality is under development.
 * </p>
 */
public class CircleMenu extends CirclePopupMenu {

    /** The context of the menu. */
    private Block block;

    public CircleMenu(Block block) {
        super((StackPane) block, null);
        this.block = block;

        // Define menu items

        // Cut Option
        MenuItem delete = new MenuItem("cut", new ImageView(new Image(this
                .getClass().getResourceAsStream("/ui/cut32.png"))));
        delete.setOnAction(t -> delete());

        // Copy Option
        MenuItem copy = new MenuItem("copy", new ImageView(new Image(this
                .getClass().getResourceAsStream("/ui/copy32.png"))));
        copy.setOnAction(t -> copy());

        // Paste Option
        MenuItem paste = new MenuItem("paste", new ImageView(new Image(this
                .getClass().getResourceAsStream("/ui/paste32.png"))));
        paste.setOnAction(t -> paste());

        // Save Option
        MenuItem save = new MenuItem("save", new ImageView(new Image(this
                .getClass().getResourceAsStream("/ui/save32.png"))));
        save.setOnAction(t -> saveBlock());

        // Registration
        this.getItems().addAll(copy, paste, delete, save);

        // Animation
        this.setAnimationInterpolation(CircularPane::animateOverTheArcWithFade);
    }

    /** Copy the {@link Block} in this context. */
    public void copy() {
        // TODO implement clipBoard in main app.
    }

    /** Paste {@link Block} from memory. */
    public void paste() {
    }

    /** Delete the {@link Block} in this context. */
    public void delete() {
        block.getPane().removeBlock(block);
    }

    /** Saves the {@link Block} in this context. */
    public void saveBlock() {
        // TODO store block in custom catalog?
    }

    /*
     * Below functions should probably be factored out to a more centralized,
     * non contextual menu. TODO implement undo and redo if time permits.
     */

    /** Undo last action. */
    public void undo() {
    }

    /** Redo last undone action. */
    public void redo() {
    }

    /** Spawn a new Function drawer. */
    public void functionDrawer() {
        // TODO spawn function list
        System.out.println("Clicked Drawer!");
    }
}
