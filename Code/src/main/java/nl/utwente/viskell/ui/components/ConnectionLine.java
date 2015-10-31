package nl.utwente.viskell.ui.components;

import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;

/**
 * This class represent a Connection-Line visual object in the UI. Each UI
 * element that uses Connection-Line properties should extend this class.
 * <p>
 * For Lines that connect inputs and outputs of Blocks see Connection.
 * </p>
 */
public class ConnectionLine extends CubicCurve implements ComponentLoader {

    /**
     * Control offset for this bezier curve of this line.
     * It determines how a far a line attempts to goes straight from its end points.
     */
    public static final double BEZIER_CONTROL_OFFSET = 150f;

    /**
     * Constructs a new ConnectionLine from FXML.
     */
    public ConnectionLine() {
        this.loadFXML("ConnectionLine");

        TactilePane.setDraggable(this, false);
        TactilePane.setGoToForegroundOnContact(this, false);
        this.setMouseTransparent(true);
    }

    /**
     * Sets the start coordinates for this ConnectionLine object.
     *
     * @param point Coordinates local to this Line's parent.
     */
    public void setStartPositionParent(Point2D point) {
        setStartX(point.getX());
        setStartY(point.getY());
        updateBezierControlPoints();
    }

    /**
     * Sets the end coordinates for this ConnectionLine object.
     *
     * @param point coordinates local to this Line's parent.
     */
    public void setEndPositionParent(Point2D point) {
        setEndX(point.getX());
        setEndY(point.getY());
        updateBezierControlPoints();
    }

    /** Returns the current bezier offset based on the current start and end positions. */
    private double getBezierYOffset() {
        double distX = Math.abs(this.getEndX() - this.getStartX());
        double distY = Math.abs(this.getEndY() - this.getStartY());
        if (distY < BEZIER_CONTROL_OFFSET) {
            if (distX < BEZIER_CONTROL_OFFSET) {
                // short lines are extra flexible
                return Math.max(BEZIER_CONTROL_OFFSET/10, Math.max(distX, distY));
            } else {
                return BEZIER_CONTROL_OFFSET;
            }
        } else {
            return Math.cbrt(distY / BEZIER_CONTROL_OFFSET) * BEZIER_CONTROL_OFFSET;
        }
    }

    /**
     * Updates the Bezier offset (curviness) according to the current start and
     * end positions.
     */
    private void updateBezierControlPoints() {
        setControlX1(getStartX());
        setControlY1(getStartY() + getBezierYOffset());
        setControlX2(getEndX());
        setControlY2(getEndY() - getBezierYOffset());
    }
}
