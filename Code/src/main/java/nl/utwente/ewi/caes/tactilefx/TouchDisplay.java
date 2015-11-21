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
import javafx.scene.shape.Line;

class TouchDisplay extends Pane {

    final static Color COLOR_OPAQUE = new Color(1.0, 0, 0, 1);
    final static Color COLOR_SEMI_TRANSPARENT = new Color(1.0, 0, 0, 0.5);
    final static double touchCircleRadius = 50.0;
    
    private final StringProperty labelText;
    private final Circle circle;
    private final Label label;
    private final Line line;
    private final int touchId;

    public TouchDisplay(double x, double y, Bounds bounds, int touchId) {
        this.touchId = touchId;
        labelText = new SimpleStringProperty(createLabelText(x, y, touchId));

        circle = new Circle(touchCircleRadius, touchCircleRadius, touchCircleRadius);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(COLOR_SEMI_TRANSPARENT);
        getChildren().add(circle);

        label = new Label();
        label.textProperty().bindBidirectional(labelText);
        label.setTextFill(COLOR_OPAQUE);
        label.relocate(0, touchCircleRadius * 2);
        getChildren().add(label);
        
        line = new Line(x, y, bounds.getMinX(), bounds.getMinY());
        getChildren().add(line);

        setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
    }

    public void relocate(double x, double y, Bounds bounds) {
        labelText.set(createLabelText(x, y, touchId));
        line.setStartX(x);
        line.setStartY(y);
        line.setEndX(bounds.getMinX());
        line.setEndY(bounds.getMinY());
        Bounds cb = circle.getBoundsInParent();
        super.relocate(x - cb.getMinX() - cb.getWidth() / 2, y - cb.getMinY() - cb.getHeight() / 2);
    }

    private String createLabelText(double x, double y, int touchId) {
        return String.format("ID=%d, x=%f, y=%f", touchId, x, y);
    }
}
