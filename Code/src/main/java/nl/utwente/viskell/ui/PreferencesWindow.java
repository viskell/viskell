package nl.utwente.viskell.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * A window for setting preferences.
 */
public class PreferencesWindow extends BorderPane implements ComponentLoader {
    private Stage stage;
    private CustomUIPane pane;

    public PreferencesWindow(CustomUIPane pane) {
        super();

        this.pane = pane;

        stage = new Stage();
        stage.setTitle("Preferences");
        stage.setScene(new Scene(this));
        loadFXML("PreferencesWindow");
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.hide();
    }
}
