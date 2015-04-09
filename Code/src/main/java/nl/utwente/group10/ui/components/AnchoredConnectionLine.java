package nl.utwente.group10.ui.components;

import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;

/**
 * This is a ConnectionLine that also stores a startAnchor and an endAnchor to keep track
 * of origin points of the Line.
 * 
 * For Lines that connect inputs and outputs of Blocks see Connection.
 */
//TODO remove this sub-extension? (Keep just ConnectionLine and Connection)
public class AnchoredConnectionLine extends ConnectionLine {
	/** Starting point of this Line that can be Anchored onto other objects */
	private ConnectionAnchor startAnchor;
	/** Ending point of this Line that can be Anchored onto other objects */
	private ConnectionAnchor endAnchor;
	
	/**
	 * Set the startAnchor for this line.
	 * After setting the StartPosition will be updated.
	 * @param Anchor to start at.
	 */
	public void setStartAnchor(ConnectionAnchor start) {
		startAnchor = start;
		updateStartPosition();
	}
	
	/**
	 * Set the endAnchor for this line.
	 * After setting the EndPosition will be updated.
	 * @param Anchor to end at.
	 */
	public void setEndAnchor(ConnectionAnchor end) {
		endAnchor = end;
		updateEndPosition();
	}
	
	/**
	 * Runs both the update Start end End position functions.
	 * Use when refreshing UI representation of the Line.
	 */
	protected void updateStartEndPositions() {
		updateStartPosition();
		updateEndPosition();
	}
	
	/**
	 * Refresh the Start position of this Line using
	 * startAnchor as a reference point.
	 */
	private void updateStartPosition() {
		double x = startAnchor.getCenterX();
		double y = startAnchor.getCenterY();
		Point2D point = startAnchor.localToScene(x, y);

		setStartPosition(point.getX(), point.getY());
	}
	
	/**
	 * Refresh the End position of this Line using
	 * endAnchor as a reference point.
	 */
	private void updateEndPosition() {
		double x = endAnchor.getCenterX();
		double y = endAnchor.getCenterY();
		Point2D point = endAnchor.localToScene(x, y);

		setEndPosition(point.getX(), point.getY());
	}
}
