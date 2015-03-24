package nl.utwente.group10.ui.components;

import java.io.IOException;

import nl.utwente.cs.caes.tactile.fxml.TactileBuilderFactory;
import nl.utwente.group10.ui.Main;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.shape.Line;

/**
 * This class represents a connection between two different FunctionBlocks. The
 * output of one FunctionBlock will be used as input for another FunctionBlock
 */
public class Connection extends CubicCurve implements ChangeListener<Number> {

	public static final double BEZIER_CONTROL_OFFSET_Y = 100f;
	
	private ConnectionAnchor startAnchor;
	private ConnectionAnchor endAnchor;
	/** The Block that inputs data into this connection */
	private Block input;
	/** The Block that we output data into from this connection */
	private Block output;
	/** The argument field of the FunctionBlock that we are outputting data into */
	private int outputarg;

	/**
	 * Method that creates a newInstance of this class along with it's visual
	 * representation.
	 * @param the FunctionBlock that inputs data into this connection
	 * @param the FunctionBlock that we output data into from this connection
	 * @param the number of the argument field to output data into
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static Connection newInstance(ConnectionAnchor startAnchor, ConnectionAnchor endAnchor) throws IOException {
		Connection connection = (Connection) FXMLLoader.load(Main.class.getResource("/ui/Connection.fxml"), null, new TactileBuilderFactory());			
		startAnchor.layoutXProperty().addListener(connection);
		startAnchor.layoutYProperty().addListener(connection);
		endAnchor.layoutXProperty().addListener(connection);
		endAnchor.layoutYProperty().addListener(connection);
		connection.startAnchor = startAnchor;
		connection.endAnchor = endAnchor;			
		connection.updateStartEndPositions();
		
		return connection;
	}
	
	
	public void setStartPosition(double x, double y) {
		this.setStartX(x);
		this.setStartY(y);
		this.setControlX1(x);
		this.setControlY1(y+BEZIER_CONTROL_OFFSET_Y);
	}
	
	public void setEndPosition(double x, double y) {
		this.setEndX(x);
		this.setEndY(y);
		this.setControlX2(x);
		this.setControlY2(y-BEZIER_CONTROL_OFFSET_Y);
	}
	

	/**
	 * Private method that sets the input into this Connection
	 * @param input source
	 */
	private void setInput(FunctionBlock in) {
		input = in;
	}

	/**
	 * Private method that sets the FunctionBlock and argument 
	 * that Connection will output into.
	 * @param FunctionBlock to output into
	 * @param Argument field to output into
	 */
	private void setOutput(FunctionBlock out, int outarg) {
		output = out;
		outputarg = outarg;
		out.setArgument(outputarg, ((FunctionBlock)input).executeMethod());
	}
	
	/**
	 * @return Block that is being used as input
	 */
	public Block getInputFunction() {
		return input;
	}
	
	/**
	 * @return Block that is being used as output
	 */
	public Block getOutputFunction() {
		return output;
	}

	@Override
	public void changed(ObservableValue<? extends Number> observable,
			Number oldValue, Number newValue) {
		
		updateStartEndPositions();
	}
	
	private void updateStartEndPositions() {
		updateStartPosition();
		updateEndPosition();
	}
	
	private void updateStartPosition() {
		setStartPosition(startAnchor.getLayoutX()+startAnchor.getCenterX()-this.getLayoutX(),startAnchor.getLayoutY()+startAnchor.getCenterY()-this.getLayoutY());
	}
	private void updateEndPosition() {
		setEndPosition(endAnchor.getLayoutX()+endAnchor.getCenterX()-this.getLayoutX(),endAnchor.getLayoutY()+endAnchor.getCenterY()-this.getLayoutY());
	}
}