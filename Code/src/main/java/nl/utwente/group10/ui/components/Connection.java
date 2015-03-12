package nl.utwente.group10.ui.components;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;

/**
 * This class represents a connection between two different FunctionBlocks. The
 * output of one FunctionBlock will be used as input for another FunctionBlock
 */
public class Connection extends StackPane {

	/** The FunctionBlock that inputs data into this connection */
	private FunctionBlock input;
	/** The FunctionBlock that we output data into from this connection */
	private FunctionBlock output;
	/** The argument field of the FunctionBlock that we are outputting data into */
	private int outputarg;

	/**
	 * Method that creates a newInstance of this class along with it's visual
	 * representation
	 * 
	 * @param the
	 *            FunctionBlock that inputs data into this connection
	 * @param the
	 *            FunctionBlock that we output data into from this connection
	 * @param the
	 *            number of the argument field to output data into
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static Connection newInstance(FunctionBlock in, FunctionBlock out,
			int outarg) {
		// TODO instantiate FXML instead of blank new connection
		Connection connection = new Connection();
		connection.setInput(in);
		connection.setOutput(out, outarg);

		return connection;
	}

	// Private method to set the input of this line
	private void setInput(FunctionBlock in) {
		input = in;
	}

	// Private method to set the functionBlock where the input will be directed
	// to
	// AKA the output
	private void setOutput(FunctionBlock out, int outarg) {
		output = out;
		outputarg = outarg;
		out.setArgument(outputarg, input.executeMethod());
	}
}
