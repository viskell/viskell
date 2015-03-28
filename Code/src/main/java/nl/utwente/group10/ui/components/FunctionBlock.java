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

	public FunctionBlock(int numArgs, CustomUIPane pane) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/FunctionBlock.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		name = new SimpleStringProperty("Function name");
		type = new SimpleStringProperty("Function type");

		cg = new CustomGesture(this, this);
		cup = pane;
		
		initializeArguments(numArgs);

		fxmlLoader.load();
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

	public String getName() {
		return name.get();
	}
	
	public void setName(String name) {
		this.name.set(name);
	}

	public String getType() {
		return type.get();
	}

	public void setType(String type) {
		this.type.set(type);
	}
}
