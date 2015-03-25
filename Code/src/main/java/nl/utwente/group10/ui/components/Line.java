package nl.utwente.group10.ui.components;

import javafx.scene.shape.CubicCurve;

/**
 * This class represent a Line visual object in the UI.
 * Each UI element that uses Line properties should extend
 * from here.
 * 
 * Each Line also stores a startAnchor and an endAnchor to keep track
 * of origin points of the Line.
 * 
 * For Lines that connect inputs and outputs of Blocks see Connection.
 */
public class Line extends CubicCurve {

	/** Control offset for this bezier of this line */
	public static final double BEZIER_CONTROL_OFFSET_Y = 100f;
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
	 * Sets the start position for this Line object
	 * @param x coordinate
	 * @param y coordinate
	 */
	public void setStartPosition(double x, double y) {
		setStartX(x);
		setStartY(y);
		setControlX1(x);
		setControlY1(y+BEZIER_CONTROL_OFFSET_Y);
	}
	
	/**
	 * Sets the end position for this Line object.
	 * @param x coordinate
	 * @param y coordinate
	 */
	public void setEndPosition(double x, double y) {
		setEndX(x);
		setEndY(y);
		setControlX2(x);
		setControlY2(y-BEZIER_CONTROL_OFFSET_Y);
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
		setStartPosition(startAnchor.getLayoutX()+startAnchor.getCenterX()-this.getLayoutX(),startAnchor.getLayoutY()+startAnchor.getCenterY()-this.getLayoutY());
	}
	
	/**
	 * Refresh the End position of this Line using
	 * endAnchor as a reference point.
	 */
	private void updateEndPosition() {
		setEndPosition(endAnchor.getLayoutX()+endAnchor.getCenterX()-this.getLayoutX(),endAnchor.getLayoutY()+endAnchor.getCenterY()-this.getLayoutY());
	}
}
