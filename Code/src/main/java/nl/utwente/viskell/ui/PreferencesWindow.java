package nl.utwente.viskell.ui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class PreferencesWindow extends BorderPane implements ComponentLoader {
    private Stage stage;
    private CustomUIPane pane;

    @FXML private ComboBox<String> theme;
    @FXML private ComboBox<String> ghci;

    public PreferencesWindow(CustomUIPane pane) {
        super();

        this.pane = pane;

        stage = new Stage();
        stage.setTitle("Inspect");
        stage.setScene(new Scene(this));
        loadFXML("PreferencesWindow");

        theme.getItems().setAll(Themes.themes.keySet());
        theme.getSelectionModel().select(Themes.name());
        theme.valueProperty().addListener(x -> Themes.setTheme(theme.getValue()));

        ghci.getItems().setAll("Default");
        ghci.getSelectionModel().select(0);
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.hide();
    }
}
