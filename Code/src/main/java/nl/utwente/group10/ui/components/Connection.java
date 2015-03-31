package nl.utwente.group10.ui.components;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

/**
 * This class represents a one-way connection between two different FunctionBlocks. The output of the first (input)
 * FunctionBlock will be used as input for the second (output) FunctionBlock.
 */
public class Connection extends Line implements ChangeListener<Number> {
	/** The Block that acts as input for this connections. */
	private Block input;

	/** The Block that we output data to from this connection. */
	private Block output;

	/**
	 * Method that creates a new instance of this class along with it's visual
	 * representation.
	 * @param from Anchor to start from.
	 * @param to Anchor to end at.
	 * @throws IOException when the FXML definition cannot be loaded.
	 */
	public Connection(OutputAnchor from, InputAnchor to) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/Connection.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		input = from.getBlock();
		output = to.getBlock();

		input.layoutXProperty().addListener(this);
		input.layoutYProperty().addListener(this);

		output.layoutXProperty().addListener(this);
		output.layoutYProperty().addListener(this);

		this.setStartAnchor(from);
		this.setEndAnchor(to);
		
		fxmlLoader.load();

		updateStartEndPositions();
	}
	
	/**
	 * @return Block that is being used as input.
	 */
	public final Block getInputBlock() {
		return input;
	}
	
	/**
	 * @return Block that is being used as output.
	 */
	public final Block getOutputBlock() {
		return output;
	}

	@Override
	public void changed(ObservableValue<?extends Number> observable, Number oldValue, Number newValue) {
		updateStartEndPositions();
	}
}
