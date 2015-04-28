package nl.utwente.group10.ui.components.lines;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.group10.ui.components.ComponentLoader;

/**
 * This class represent a Connection-Line visual object in the UI. Each UI
 * element that uses Connection-Line properties should extend this class.
 *
 * For Lines based on Start and End anchors, see AnchoredConnectionLine For
 * Lines that connect inputs and outputs of Blocks see Connection.
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
     * Sets the start position for this Line object
     * @param x coordinate
     * @param y coordinate
     */
    public void setStartPosition(double x, double y) {
        setStartX(x);
        setStartY(y);
        updateBezierControlPoints();
    }

    public void setStartPosition(Point2D point) {
        setStartPosition(point.getX(), point.getY());
    }

    /**
     * Sets the end position for this Line object.
     * @param x coordinate
     * @param y coordinate
     */
    public void setEndPosition(double x, double y) {
        setEndX(x);
        setEndY(y);
        updateBezierControlPoints();
    }

    public void setEndPosition(Point2D point) {
        setEndPosition(point.getX(), point.getY());
    }

    /**
     * Updates the Bezier offset (curviness) according to the current start and end positions.
     */
    private void updateBezierControlPoints() {
        setControlX1(getStartX());
        setControlY1(getStartY() + getCurrentBezierOffset());
        setControlX2(getEndX());
        setControlY2(getEndY() - getCurrentBezierOffset());
    }

    /**
     * @return The current bezier offset based on the current start and end positions.
     */
    private double getCurrentBezierOffset() {
        double unclamped = BEZIER_CONTROL_OFFSET_MULTIPLIER
                * getStraightLineLength();
        return Math.max(Math.min(unclamped, BEZIER_CONTROL_OFFSET_MAXIMUM),
                BEZIER_CONTROL_OFFSET_MINIMUM);
    }

    /**
     * @return The length of a direct line between the end and start point of
     *         this CubicCurve
     */
    public double getStraightLineLength() {
        return Math.sqrt(getDeltaX() * getDeltaX() + getDeltaY() * getDeltaY());
    }

    public double getDeltaX() {
        return this.getEndX() - this.getStartX();
    }

    public double getDeltaY() {
        return this.getEndY() - this.getStartY();
    }
    
    /**
     * @returns a point which lies in the middle between the start point and the end point.
     */
    public Point2D getMidPoint() {
        Point2D start = new Point2D(this.getStartX(), this.getStartY());
        Point2D end = new Point2D(this.getEndX(), this.getEndY());
        
        return start.midpoint(end);
        
    }
}
