package nl.utwente.group10.ui.menu;

import javafx.util.Duration;

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
        ImageView image = makeImageView("/ui/cut32.png");
        MenuItem delete = new MenuItem("cut", image);
        delete.setOnAction(t -> delete());

        // Copy Option
        image = makeImageView("/ui/copy32.png");
        MenuItem copy = new MenuItem("copy", image);
        copy.setOnAction(t -> copy());

        // Paste Option
        image = makeImageView("/ui/paste32.png");
        MenuItem paste = new MenuItem("paste", image);
        paste.setOnAction(t -> paste());

        // Save Option
        image = makeImageView("/ui/save32.png");
        MenuItem save = new MenuItem("save", image);
        save.setOnAction(t -> saveBlock());

        // Registration
        this.getItems().addAll(copy, paste, delete, save);

        // Animation
        this.setAnimationInterpolation(CircularPane::animateOverTheArcWithFade);
        this.setAnimationDuration(Duration.ONE);
    }
    
    private ImageView makeImageView(String path) {
        ImageView image = new ImageView(new Image(this.getClass().getResourceAsStream(path)));
        image.getStyleClass().add("hoverMenu");
        return image;
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
}
