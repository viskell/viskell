package nl.utwente.group10.ui.components;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import nl.utwente.group10.ui.CustomUIPane;

public class CustomAlert extends Pane implements ComponentLoader {
    @FXML
    private Text text;

    private CustomUIPane pane;

    public CustomAlert(CustomUIPane pane, String message) {
        this.loadFXML("Alert");

        this.pane = pane;
        text.setText(message);
    }

    @FXML
    private void close(MouseEvent event) {
        this.pane.getChildren().remove(this);
    }
}
