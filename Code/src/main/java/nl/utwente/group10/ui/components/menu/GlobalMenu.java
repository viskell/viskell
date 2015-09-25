package nl.utwente.group10.ui.components.menu;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * A context menu with global actions (i.e. quit).
 */
public class GlobalMenu extends ContextMenu {
    public GlobalMenu() {
        super();

        MenuItem menuQuit = new MenuItem("Quit");
        menuQuit.setOnAction(e -> Platform.exit());

        this.getItems().addAll(menuQuit);
    }
}
