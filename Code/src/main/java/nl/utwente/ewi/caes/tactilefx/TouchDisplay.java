package nl.utwente.ewi.caes.tactilefx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

class TouchDisplay extends Pane {

    static Color COLOR_OPAQUE = new Color(1.0, 0, 0, 1);
    static Color COLOR_SEMI_TRANSPARENT = new Color(1.0, 0, 0, 0.5);

    private final StringProperty labelText;
    private final Circle circle;
    private final Label label;
    private final int touchId;

    public TouchDisplay(double x, double y, double radius, int touchId) {
        this.touchId = touchId;
        labelText = new SimpleStringProperty(createLabelText(x, y, touchId));

        circle = new Circle(radius, radius, radius);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(COLOR_SEMI_TRANSPARENT);
        getChildren().add(circle);

        label = new Label();
        label.textProperty().bindBidirectional(labelText);
        label.setTextFill(COLOR_OPAQUE);
        label.relocate(0, radius * 2);
        getChildren().add(label);

        setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
    }

    @Override
    public void relocate(double x, double y) {
        labelText.set(createLabelText(x, y, touchId));

        Bounds cb = circle.getBoundsInParent();
        super.relocate(x - cb.getMinX() - cb.getWidth() / 2, y - cb.getMinY() - cb.getHeight() / 2);
    }

    private String createLabelText(double x, double y, int touchId) {
        return String.format("ID=%d, x=%f, y=%f", touchId, x, y);
    }
}
