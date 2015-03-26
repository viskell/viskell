package nl.utwente.group10.ui.components;

import java.awt.Color;
import java.io.IOException;

import nl.utwente.cs.caes.tactile.fxml.TactileBuilderFactory;
import nl.utwente.group10.ui.Main;
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
	/** The output of this FunctionBlock.**/
	private ConnectionAnchor output;
	/** The inputs for this FunctionBlock.**/
	private ConnectionAnchor[] inputs;
	
	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 * @param the number of arguments this FunctionBlock can hold
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static FunctionBlock newInstance(int numberOfArguments) throws IOException {
		FunctionBlock functionBlock = (FunctionBlock) FXMLLoader.load(Main.class.getResource("/ui/FunctionBlock.fxml"), null, new TactileBuilderFactory());
		functionBlock.initializeArguments(numberOfArguments);

		//TODO put in new method
		ConnectionAnchor newAnchor = ConnectionAnchor.newInstance();
		Pane anchorSpace = (Pane) functionBlock.lookup("#output_anchor_space");
		anchorSpace.getChildren().add(newAnchor);
		return functionBlock;
	}
	
	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 * @param the number of arguments this FunctionBlock can hold
	 * @param the name of this FunctionBlock
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static FunctionBlock newInstance(int numberOfArguments, String name) throws IOException {
		FunctionBlock functionBlock = newInstance(numberOfArguments);
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
	 * Each argument will generate a corresponding ConnectionAnchor where input can be linked to.
	 * @param numberOfArguments
	 * @throws IOException 
	 */
	private void initializeArguments(int numberOfArguments) throws IOException {
		arguments = new String[numberOfArguments];
		inputs = new ConnectionAnchor[numberOfArguments];
		
		// create anchors for each argument and 'anchor' them around the functionBlock
		for(int i = 0;i<numberOfArguments; i++){
			ConnectionAnchor newAnchor = ConnectionAnchor.newInstance();
			inputs[i] = newAnchor;
			Pane anchorSpace = (Pane) this.lookup("#input_anchor_space");
			anchorSpace.getChildren().add(newAnchor);
			Pane argumentSpace = (Pane) this.lookup("#argument_space");
			//TODO get css styling on these arguments
			//Label argument = new Label("Integer");
		    //argumentSpace.getChildren().add(argument);
		}
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
	
	/**
	 * Method that returns the name of this Function.
	 * @return functionName
	 */
	public String getName() {
		return functionName;
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
