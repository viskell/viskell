package nl.utwente.group10.ui.components;

import java.io.IOException;

import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;
import nl.utwente.group10.ui.Main;
import nl.utwente.group10.ui.components.Line;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;

/**
 * This class represents a connection between two different FunctionBlocks. The
 * output of one FunctionBlock will be used as input for another FunctionBlock
 */
public class Connection extends Line implements ChangeListener<Number> {

	/** The Block that inputs data into this connection */
	private Block input;
	/** The Block that we output data into from this connection */
	private Block output;
	/** The argument field of the FunctionBlock that we are outputting data into */
	private int outputarg;
	
	/**
	 * Method that creates a newInstance of this class along with it's visual
	 * representation.
	 * @return a new empty instance of this class
	 * @throws IOException
	 */
	private static Connection newInstance() throws IOException {
		Connection connection = (Connection) FXMLLoader.load(Main.class.getResource("/ui/Connection.fxml"), null, new TactileBuilderFactory());					
		return connection;
	}

	/**
	 * Method that creates a newInstance of this class along with it's visual
	 * representation. 
	 * @param Anchor of the Block
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static Connection newInstance(ConnectionAnchor startAnchor) throws IOException {
		Connection connection = newInstance();
		startAnchor.layoutXProperty().addListener(connection);
		startAnchor.layoutYProperty().addListener(connection);
		
		ConnectionAnchor endAnchor = ConnectionAnchor.newInstance();
		endAnchor.layoutXProperty().addListener(connection);
		endAnchor.layoutYProperty().addListener(connection);
		
		connection.setStartAnchor(startAnchor);
		connection.setEndAnchor(endAnchor);	
		
		return connection;
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
	public void changed(ObservableValue<?extends Number> observable, Number oldValue, Number newValue) {
		updateStartEndPositions();
	}
}