package nl.utwente.group10.ui.components.lines;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;

import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.group10.ui.components.ComponentLoader;

/**
 * This class represent a Connection-Line visual object in the UI. Each UI
 * element that uses Connection-Line properties should extend this class.
 * <p>
 * For Lines that connect inputs and outputs of Blocks see Connection.
 * </p>
 */
public class ConnectionLine extends CubicCurve implements ComponentLoader {

    /**
     * Control offset for this bezier of this line. in simple terms: controls
     * the curviness of the line.
     *
     * A higher offset equates to a more curved line. The offset is a function
     * of the offset multiplier and the straight line length. The offset will
     * always be between the defined minimum and maximum.
     */
    public static final double BEZIER_CONTROL_OFFSET_MULTIPLIER = 0.7f;
    public static final double BEZIER_CONTROL_OFFSET_MINIMUM = 10f;
    public static final double BEZIER_CONTROL_OFFSET_MAXIMUM = 200f;

    /** The fxmlLoader responsible for loading the fxml. */
    private FXMLLoader fxmlLoader;

    public ConnectionLine() {
        try {
            getFXMLLoader("ConnectionLine").load();
        } catch (IOException e) {
            // TODO Find a good way to handle this
            e.printStackTrace();
        }

        TactilePane.setDraggable(this, false);
        TactilePane.setGoToForegroundOnContact(this, false);
        this.setMouseTransparent(true);
    }

    /**
     * Sets the start coordinates for this Line object.
     * 
     * @param xCoord
     * @param yCoord
     */
    public void setStartPosition(double xCoord, double yCoord) {
        setStartX(xCoord);
        setStartY(yCoord);
        updateBezierControlPoints();
    }

    public void setStartPosition(Point2D point) {
        setStartPosition(point.getX(), point.getY());
    }

    /**
     * Sets the end coordinate for this Line object.
     * 
     * @param xCoord
     * @param yCoord
     */
    public void setEndPosition(double xCoord, double yCoord) {
        setEndX(xCoord);
        setEndY(yCoord);
        updateBezierControlPoints();
    }

    public void setEndPosition(Point2D point) {
        setEndPosition(point.getX(), point.getY());
    }

    /**
     * Returns the current bezier offset based on the current start and end
     * positions.
     */
    private double getCurrentBezierOffset() {
        double unclamped = BEZIER_CONTROL_OFFSET_MULTIPLIER
                * getStraightLineLength();
        return Math.max(Math.min(unclamped, BEZIER_CONTROL_OFFSET_MAXIMUM),
                BEZIER_CONTROL_OFFSET_MINIMUM);
    }

    /**
     * Returns the length of a direct line between the end and start point of
     * this CubicCurve
     */
    public double getStraightLineLength() {
        return Math.sqrt(getDeltaX() * getDeltaX() + getDeltaY() * getDeltaY());
    }

    /** Returns the difference between the start and end X posistion. */
    public double getDeltaX() {
        return this.getEndX() - this.getStartX();
    }

    /** Returns the difference between the start and end Y posistion. */
    public double getDeltaY() {
        return this.getEndY() - this.getStartY();
    }

    /**
     * Updates the Bezier offset (curviness) according to the current start and
     * end positions.
     */
    private void updateBezierControlPoints() {
        setControlX1(getStartX());
        setControlY1(getStartY() + getCurrentBezierOffset());
        setControlX2(getEndX());
        setControlY2(getEndY() - getCurrentBezierOffset());
    }
}
