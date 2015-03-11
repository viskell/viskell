package nl.utwente.cs.caes.tactile.debug;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

class VectorDisplay extends Pane {

    static Color COLOR_OPAQUE = new Color(0, 0, 1, 1);
    static Color COLOR_SEMI_TRANSPARENT = new Color(0, 0, 1, 0.5);
    static final double OFFSET = 10;

    private final Line line = new Line(0, 0, 0, 0);
    private final Label label = new Label("");
    private final ObjectProperty<Point2D> vectorProperty;

    public VectorDisplay(ObjectProperty<Point2D> vectorProperty) {
        this.vectorProperty = vectorProperty;

        line.setStroke(COLOR_OPAQUE);
        line.setStrokeWidth(2);
        label.setTextFill(COLOR_OPAQUE);

        getChildren().add(line);
        getChildren().add(label);

        vectorProperty.addListener(observable -> {
            update();
        });

        update();
    }

    private void update() {
        double x = vectorProperty.get().getX();
        double y = vectorProperty.get().getY();

        if (x == 0 && y == 0) {
            line.setVisible(false);
            label.setText("");
            return;
        }

        line.setVisible(true);
        double labelOffsetX, labelOffsetY;

        line.setEndX(x);
        line.setEndY(y);
        label.setText(String.format("%1.2f, %1.2f", x, y));

        labelOffsetX = x > 0 ? OFFSET : -OFFSET - label.getWidth();
        labelOffsetY = y > 0 ? OFFSET : -OFFSET - label.getHeight();

        label.relocate(x / 2 + labelOffsetX, y / 2 + labelOffsetY);
    }
}
