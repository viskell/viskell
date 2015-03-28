package nl.utwente.group10.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import java.io.IOException;

import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.Main;
import nl.utwente.group10.ui.gestures.CustomGesture;
import nl.utwente.group10.ui.gestures.UIEvent;
import nl.utwente.group10.ui.gestures.GestureCallBack;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * Main building block for the visual interface, this class
 * represents a Haskell function together with it's arguments and
 * visual representation.
 */
public class FunctionBlock extends Block {
	/** The arguments this FunctionBlock holds.**/
	private String[] arguments;

	/** The name of this Function. **/
	private StringProperty name;

	/** The type of this Function. **/
	private StringProperty type;
	
	/** intstance to create Events for this FunctionBlock. **/
	private static CustomGesture cg;

	@FXML
	private Pane nestSpace;

	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 * @param the number of arguments this FunctionBlock can hold
	 * @param pane: The CustomUIPane in which this FunctionBlock exists. Via this this FunctionBlock knows which other FunctionBlocks exist.
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public FunctionBlock(int numArgs, CustomUIPane pane) throws IOException {
		super("FunctionBlock", pane);
		
		name = new SimpleStringProperty("Function name");
		type = new SimpleStringProperty("Function type");
		
		cg = new CustomGesture(this, this);
		
		initializeArguments(numArgs);
		
		this.getLoader().load();
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
	 * Private method to initialize the argument fields for this function block.
	 * All arguments will are defined as Strings and will be stored as such.
	 * An Integer value of 6 will also be stored as a String "6"
	 * @param numberOfArguments
	 */
	private void initializeArguments(int numberOfArguments) {
		arguments = new String[numberOfArguments];
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
	 * Set the value of the name property of this FunctionBlock.
	 * @param name
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
	 * Set the type property of this FunctionBlock.
	 * @param type
	 */
	public void setType(String type) {
		this.type.set(type);
	}
}
