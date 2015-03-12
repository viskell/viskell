package nl.utwente.group10.ui;

import java.io.IOException;

import nl.utwente.cs.caes.tactile.fxml.TactileBuilderFactory;
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
public class FunctionBlock extends StackPane{
	//The arguments this FunctionBlock holds
	private String[] arguments;
	private String functionName;
	private boolean isSelected = false;
	
	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 * @param the number of arguments this FunctionBlock can hold
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static FunctionBlock newInstance(int numberOfArguments) throws IOException{
		//TODO Prettier resource loading
		//TODO better factory
		FunctionBlock functionBlock = (FunctionBlock) FXMLLoader.load(Main.class.getResource("FunctionBlock.fxml"), null, new TactileBuilderFactory());
		functionBlock.initializeArguments(numberOfArguments);
		
		return functionBlock;
	}
	
	//TODO To be implemented method that will parse code into Haskell interface
	// and returns the result
	public String executeMethod(){		
		return new String("DEBUG-OUTPUT");
	}
	
	public void nest(Node node){
		Pane nestSPace = (Pane) this.lookup("#nest_space");
		((Label) this.lookup("#label_function_name")).setText("Trolololol");
		nestSPace.getChildren().add(node);
	}
	
	//Private method to initialize the argument fields for this function block.
	//All arguments will be stored as Strings.
	private void initializeArguments(int numberOfArguments){
		arguments = new String[numberOfArguments];
	}
	
	/**
	 * Method to set the value of a specified argument
	 * @param the index of the argument field
	 * @param the value that the argument should be changed to
	 */
	public void setArgument(int i,String arg){
		arguments[i] = arg;
	}
	
	//Selects or deselects this FunctionBlock
	public void setSelected(boolean bool){
		//If another object is selected then deselect it first!!
		isSelected = bool;
	}
	
	/**
	 * Method to set the name of this FunctionBlock
	 * @param name
	 */
	public void setName(String name){
		functionName = name;
		Label label = ((Label)this.lookup("#label_function_name"));
		label.setText(functionName);
	}
}
