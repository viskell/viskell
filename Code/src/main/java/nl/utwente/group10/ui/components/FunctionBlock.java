package nl.utwente.group10.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

import java.io.IOException;

import javafx.scene.control.Label;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.gestures.CustomGesture;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Main building block for the visual interface, this class
 * represents a Haskell function together with it's arguments and
 * visual representation.
 */
public class FunctionBlock extends Block {
	/** The arguments this FunctionBlock holds.**/
	private String[] arguments;
	
	/** The inputs for this FunctionBlock.**/
	private ConnectionAnchor[] inputs;

	/** The types of the input for this FunctionBlock. **/
	private Label[] labels;
	
	/** The name of this Function. **/
	private StringProperty name;

	/** The type of this Function. **/
	private StringProperty type;

	/** intstance to create Events for this FunctionBlock. **/
	private static CustomGesture cg;

	@FXML private Pane nestSpace;
	
	@FXML private Pane anchorSpace;
	
	@FXML private Pane outputSpace;
	
	@FXML private Pane argumentSpace;

	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 * @param numArgs The number of arguments this FunctionBlock can hold
	 * @param pane The CustomUIPane in which this FunctionBlock exists. Via this this FunctionBlock knows which other FunctionBlocks exist.
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public FunctionBlock(int numArgs, CustomUIPane pane) throws IOException {
		super("FunctionBlock", pane);
		
		name = new SimpleStringProperty("Function name");
		type = new SimpleStringProperty("Function type");

		cg = new CustomGesture(this, this);
		
		this.getLoader().load();
		
		outputSpace.getChildren().add(this.getOutputAnchor());

		arguments = new String[numArgs];
		inputs = new ConnectionAnchor[numArgs];
		labels = new Label[numArgs];

		// Create anchors and labels for each argument
		for (int i = 0; i < numArgs; i++) {
			arguments[i] = "Int";

			inputs[i] = new ConnectionAnchor();
			anchorSpace.getChildren().add(inputs[i]);

			labels[i] = new Label(String.format(" %s ", arguments[i]));
			labels[i].getStyleClass().add("argument");
			argumentSpace.getChildren().add(labels[i]);
		}
	}
	
	/**
	 * Executes this FunctionBlock and returns the output as a String
	 * @return Output of the Function
	 */
	public String executeMethod() {		
		return new String("DEBUG-OUTPUT");
	}
	
	/**
	 * Nest another Node object within this FunctionBlock
	 * @param node to nest
	 */
	public void nest(Node node) {
		name.set("Higher order function");
		nestSpace.getChildren().add(node);
	}

	/**
	 * Method to set the value of a specified argument
	 */
	public void setArgument(int i,String arg) {
		arguments[i] = arg;
	}

	/**
	 * Get the name property of this FunctionBlock.
	 * @return name
	 */
	public String getName() {
		return name.get();
	}
	
	/**
	 * @param name for this FunctionBlock
	 */
	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * Get the type property of this FunctionBlock.
	 * @return type
	 */
	public String getType() {
		return type.get();
	}

	/**
	 * @param type the type property of this FunctionBlock.
	 */
	public void setType(String type) {
		this.type.set(type);
	}
	
	/**
	 * the StringProperty for the name of this FunctionBlock.
	 * @return name
	 */
	public StringProperty nameProperty() {
		return name;
	}
	
	/**
	 * the StringProperty for the type of this FunctionBlock.
	 * @return type
	 */
	public StringProperty typeProperty() {
		return type;
	}
	
	/**
	 * Method to fetch an array containing all of the input anchors for this
	 * FunctionBlock
	 * @return inputAnchors
	 */
	public ConnectionAnchor[] getInputs(){
		return inputs;
	}
	
	/**
	 * Returns the index of the argument matched to the Anchor.
	 * @param anchor
	 * @return argumentIndex
	 */
	public int getArgumentIndex(ConnectionAnchor anchor) {
		int index=0;
		/**
		 * @invariant index < inputs.length
		 */
		while((inputs[index]!=anchor)&&(index<inputs.length)) {
			index++;
		}
		return index;
	}
	
	//TODO index? argument? or both?
	public String getArgument(ConnectionAnchor anchor) {
		int index = getArgumentIndex(anchor);
		
		return arguments[index];
	}
}
