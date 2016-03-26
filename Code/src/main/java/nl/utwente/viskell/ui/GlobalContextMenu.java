package nl.utwente.viskell.ui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * A context menu with global actions (i.e. quit).
 */
public class GlobalContextMenu extends ContextMenu {
    public GlobalContextMenu(MenuActions menuActions) {
        super();

        MenuItem menuPreferences = new MenuItem("Preferences...");
        menuPreferences.setOnAction(menuActions::showPreferences);

        MenuItem menuInspector = new MenuItem("Inspector");
        menuInspector.setOnAction(menuActions::showInspector);

        MenuItem menuFullScreen = new MenuItem("Toggle full screen");
        menuFullScreen.setOnAction(menuActions::toggleFullScreen);
        
        MenuItem menuQuit = new MenuItem("Quit");
        menuQuit.setOnAction(menuActions::onQuit);

        this.getItems().addAll(menuActions.fileMenuItems());
        this.getItems().addAll(menuInspector, menuPreferences, menuFullScreen, menuQuit);
    }
}
