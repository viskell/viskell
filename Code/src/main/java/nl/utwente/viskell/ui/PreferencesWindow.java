package nl.utwente.viskell.ui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ghcj.HaskellException;

import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * A window for setting preferences.
 */
public class PreferencesWindow extends BorderPane implements ComponentLoader {
    private Stage stage;
    private CustomUIPane pane;
    private Preferences preferences;

    @FXML private ComboBox<GhciSession.Backend> ghci;

    public PreferencesWindow(CustomUIPane customUIPane) {
        super();

        pane = customUIPane;
        preferences = Preferences.userNodeForPackage(Main.class);

        stage = new Stage();
        stage.setTitle("Preferences");
        stage.setScene(new Scene(this, 450, 450));

        loadFXML("PreferencesWindow");

        ghci.getItems().setAll(GhciSession.getBackends());
        ghci.getSelectionModel().select(GhciSession.pickBackend());
        ghci.valueProperty().addListener(x -> {
            preferences.put("ghci", ghci.getValue().toString());
            pane.restartBackend();
        });
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.hide();
    }
}
