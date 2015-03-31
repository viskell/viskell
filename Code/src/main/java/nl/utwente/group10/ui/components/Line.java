package nl.utwente.group10.ui.components;

import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;

/**
 * This class represent a Line visual object in the UI. Each UI element that uses Line properties should extend from
 * here. Each Line also stores a startAnchor and an endAnchor to keep track of origin points of the Line.
 *
 * Line is specialized into Connection if it connects two Blocks.
 */
public class Line extends CubicCurve {
	/** Control offset for this bezier of this line. */
	public static final double BEZIER_CONTROL_OFFSET_Y = 100f;
	/** Starting point of this Line that can be Anchored onto other objects. */
	private ConnectionAnchor startAnchor;
	/** Ending point of this Line that can be Anchored onto other objects. */
	private ConnectionAnchor endAnchor;

	/**
	 * Sets the start point for this line.
	 * @param start The Anchor that is the start point of this line.
	 */
	public final void setStartAnchor(ConnectionAnchor start) {
		startAnchor = start;
		updateStartPosition();
	}

	/**
	 * Sets the start position for this line and updates the end position.
	 * @param x X-coordinate.
	 * @param y Y-coordinate.
	 */
	public final void setStartPosition(double x, double y) {
		setStartX(x);
		setStartY(y);
		setControlX1(x);
		setControlY1(y + BEZIER_CONTROL_OFFSET_Y);
	}

	/**
	 * Sets the end point for this line and updates the end position.
	 * @param end The Anchor that is the end point of this line.
	 */
	public final void setEndAnchor(ConnectionAnchor end) {
		endAnchor = end;
		updateEndPosition();
	}

	/**
	 * Sets the end position for this line.
	 * @param x X-coordinate.
	 * @param y Y-coordinate.
	 */
	public final void setEndPosition(double x, double y) {
		setEndX(x);
		setEndY(y);
		setControlX2(x);
		setControlY2(y - BEZIER_CONTROL_OFFSET_Y);
	}

	/**
	 * Runs both the update start end end position functions. Used when refreshing the UI representation of the Line.
	 */
	protected final void updateStartEndPositions() {
		updateStartPosition();
		updateEndPosition();
	}

	/**
	 * Refreshes the start position of this Line with the set start anchor as start point.
	 */
	private void updateStartPosition() {
		double x = startAnchor.getCenterX();
		double y = startAnchor.getCenterY();
		Point2D point = startAnchor.localToScene(x, y);

		setStartPosition(point.getX(), point.getY());
	}

	/**
	 * Refresh the end position of this line using endAnchor as a reference point.
	 */
	private void updateEndPosition() {
		double x = endAnchor.getCenterX();
		double y = endAnchor.getCenterY();
		Point2D point = endAnchor.localToScene(x, y);

		setEndPosition(point.getX(), point.getY());
	}
}
