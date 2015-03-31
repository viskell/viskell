package nl.utwente.group10.ui.components;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.shape.CubicCurve;


public class ConnectionLine extends CubicCurve {
	/** Control offset for this bezier of this line */
	public static final double BEZIER_CONTROL_OFFSET_Y = 100f;

	/** The fxmlLoader responsible for loading the fxml.*/
	private FXMLLoader fxmlLoader;
	
	public ConnectionLine(){
		try {
			fxmlLoader = new FXMLLoader(getClass().getResource("/ui/ConnectionLine.fxml"));
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
}
