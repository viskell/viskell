package nl.utwente.group10.ui.components;

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
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;

/**
 * Main building block for the visual interface, this class
 * represents a Haskell function together with it's arguments and
 * visual representation.
 */
public class FunctionBlock extends Block {
	/** The arguments this FunctionBlock holds.**/
	private String[] arguments;
	/** The name of this Function.**/
	private String functionName;
	/** intstance to create Events for this FunctionBlock. **/
	private static CustomGesture cg;
		
	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 * @param the number of arguments this FunctionBlock can hold
	 * @param pane: The CustomUIPane in which this FunctionBlock exists. Via this this FunctionBlock knows which other FunctionBlocks exist.
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static FunctionBlock newInstance(int numberOfArguments, CustomUIPane pane) throws IOException {
		FunctionBlock functionBlock = (FunctionBlock) FXMLLoader.load(Main.class.getResource("/ui/FunctionBlock.fxml"), null, new TactileBuilderFactory());
		functionBlock.initializeArguments(numberOfArguments);
		cg = new CustomGesture(functionBlock, functionBlock);
		cup = pane;
		return functionBlock;
	}
	
	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 * @param the number of arguments this FunctionBlock can hold
	 * @param the name of this FunctionBlock
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static FunctionBlock newInstance(int numberOfArguments, CustomUIPane pane, String name) throws IOException {
		FunctionBlock functionBlock = newInstance(numberOfArguments, pane);
		functionBlock.setName(name);
		
		return functionBlock;
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
		Pane nestSpace = (Pane) this.lookup("#nest_space");
		((Label) this.lookup("#label_function_name")).setText("Higher order function");
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
	 * @param the index of the argument field
	 * @param the value that the argument should be changed to
	 */
	public void setArgument(int i,String arg) {
		arguments[i] = arg;
	}
	
	/**
	 * Method to set the name of this FunctionBlock
	 * @param name
	 */
	public void setName(String name) {
		functionName = name;
		Label label = ((Label)this.lookup("#label_function_name"));
		label.setText(functionName);
	}
}
