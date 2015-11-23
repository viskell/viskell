package nl.utwente.viskell.ui;

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

    final static Color COLOR_OPAQUE = new Color(1.0, 0, 0, 1);
    final static Color COLOR_SEMI_TRANSPARENT = new Color(1.0, 0, 0, 0.5);
    final static double touchCircleRadius = 50.0;
    
    private final StringProperty labelText;
    private final Circle circle;
    private final Label label;
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
        
        setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
    }

    /** Move this TouchDisplay to a new x-y position. */
    protected void moveTouchPoint(double x, double y) {
        labelText.set(createLabelText(x, y, touchId));
        Bounds cb = circle.getBoundsInParent();
        this.relocate(x - cb.getMinX() - cb.getWidth() / 2, y - cb.getMinY() - cb.getHeight() / 2);
    }

    private String createLabelText(double x, double y, int touchId) {
        return String.format("ID=%d, x=%f, y=%f", touchId, x, y);
    }
}
