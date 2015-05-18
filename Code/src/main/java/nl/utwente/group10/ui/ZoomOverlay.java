package nl.utwente.group10.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class ZoomOverlay extends StackPane {
    public static final Pos BUTTON_POS = Pos.BOTTOM_CENTER;

    public ZoomOverlay(Node child, CustomUIPane pane) {
        super();

        Button zoomIn = new Button("+");
        zoomIn.setOnAction(e -> pane.zoomIn());

        Button zoomOut = new Button("–");
        zoomOut.setOnAction(e -> pane.zoomOut());

        FlowPane buttons = new FlowPane(10, 0, zoomIn, zoomOut);
        buttons.setPrefSize(100, 20);
        buttons.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        buttons.setPadding(new Insets(10));
        buttons.getStyleClass().add("zoomButtons");
        StackPane.setAlignment(buttons, BUTTON_POS);

        buttons.getChildren().setAll(zoomIn, zoomOut);
        this.getChildren().setAll(child, buttons);
    }
}
