package nl.utwente.viskell.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * A top menu bar with global actions
 */
public class MacTopMenu extends MenuBar {
    public MacTopMenu(MenuActions menuActions) {
        super();

        useSystemMenuBarProperty().set(true);

        // Viskell menu items on mac - preferences and quit
        final Menu appMenu = new Menu("Viskell");
        MenuItem preferencesMenuItem = new MenuItem("Preferences");
        preferencesMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        preferencesMenuItem.setOnAction(menuActions::showPreferences);

        MenuItem quitMenuItem = new MenuItem("Quit");
        quitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
        quitMenuItem.setOnAction(menuActions::onQuit);
        appMenu.getItems().addAll(preferencesMenuItem, quitMenuItem);

        // File menu
        final Menu fileMenu = new Menu("_File");
        fileMenu.setMnemonicParsing(true);
        fileMenu.getItems().addAll(menuActions.fileMenuItems());

        // View menu
        final Menu viewMenu = new Menu("_View");
        viewMenu.setMnemonicParsing(true);
        MenuItem menuInspector = new MenuItem("Inspector");
        menuInspector.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));
        menuInspector.setOnAction(menuActions::showInspector);
        viewMenu.getItems().addAll(menuInspector);

        // Help Menu
        final Menu helpMenu = new Menu("_Help");
        helpMenu.setMnemonicParsing(true);

        getMenus().addAll(appMenu, fileMenu, viewMenu, helpMenu);
    }
}
