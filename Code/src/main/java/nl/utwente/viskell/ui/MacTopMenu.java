package nl.utwente.viskell.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

/**
 * A top menu bar with global actions
 */
public class MacTopMenu extends MenuBar {
    public MacTopMenu(MenuActions menuActions) {
        super();

        useSystemMenuBarProperty().set(true);

        // Viskell menu items on mac - preferences and quit
        MenuItem preferencesMenuItem = new MenuItem("Preferences");
        preferencesMenuItem.setOnAction(menuActions::showPreferences);

        MenuItem quitMenuItem = new MenuItem("Quit");
        quitMenuItem.setOnAction(menuActions::onQuit);

        final Menu appMenu = new Menu("Viskell");
        appMenu.getItems().addAll(preferencesMenuItem, quitMenuItem);

        // File menu
        final Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(menuActions.fileMenuItems());

        // View menu
        final Menu viewMenu = new Menu("View");
        MenuItem menuInspector = new MenuItem("Inspector");
        menuInspector.setOnAction(menuActions::showInspector);
        viewMenu.getItems().addAll(menuInspector);

        // Help Menu
        final Menu helpMenu = new Menu("Help");

        getMenus().addAll(appMenu, fileMenu, viewMenu, helpMenu);
    }
}
