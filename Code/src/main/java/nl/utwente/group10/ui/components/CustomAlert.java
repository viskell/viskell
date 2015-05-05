package nl.utwente.group10.ui.components;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import nl.utwente.group10.ui.CustomUIPane;

/**
 * Recent versions of JavaFX ship with their own Alert dialogs, but those can't
 * be added to the TactilePane directly. This CustomAlert dialog can.
 */
public class CustomAlert extends Pane implements ComponentLoader {
    @FXML
    private Text text;

    private CustomUIPane pane;

    public CustomAlert(CustomUIPane pane, String message) throws IOException {
        this.getFXMLLoader("Alert").load();
        this.pane = pane;
        text.setText(message);
    }

    @FXML
    private void close(MouseEvent event) {
        this.pane.getChildren().remove(this);
    }
}
