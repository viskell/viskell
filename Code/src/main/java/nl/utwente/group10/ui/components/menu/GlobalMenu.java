package nl.utwente.group10.ui.components.menu;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import nl.utwente.group10.ui.CustomUIPane;

/**
 * A context menu with global actions (i.e. quit).
 */
public class GlobalMenu extends ContextMenu {
    public GlobalMenu(CustomUIPane pane) {
        super();

        MenuItem menuUndo = new MenuItem("Undo");
        menuUndo.setOnAction(e -> pane.getHistory().undo());

        MenuItem menuRedo = new MenuItem("Redo");
        menuRedo.setOnAction(e -> pane.getHistory().redo());

        MenuItem menuQuit = new MenuItem("Quit");
        menuQuit.setOnAction(e -> Platform.exit());

        this.getItems().addAll(menuUndo, menuRedo, menuQuit);
    }
}
