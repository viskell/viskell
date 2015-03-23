package nl.utwente.group10.ui.components;

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
public class FunctionBlock extends Block{
	/**The arguments this FunctionBlock holds.**/
	private String[] arguments;
	/** The name of this Function.**/
	private String functionName;
	
	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 * @param the number of arguments this FunctionBlock can hold
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static FunctionBlock newInstance(int numberOfArguments){
		FunctionBlock functionBlock;
		try{
			functionBlock = (FunctionBlock) FXMLLoader.load(Main.class.getResource("/ui/FunctionBlock.fxml"), null, new TactileBuilderFactory());
			functionBlock.initializeArguments(numberOfArguments);
		}catch(IOException e){
			e.printStackTrace();
			functionBlock = null;
		}
		return functionBlock;
	}
	
	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 * @param the number of arguments this FunctionBlock can hold
	 * @param the name of this FunctionBlock
	 * @return a new instance of this class
	 * @throws IOException
	 */
	public static FunctionBlock newInstance(int numberOfArguments, String name) throws IOException{
		FunctionBlock functionBlock = newInstance(numberOfArguments);
		functionBlock.setName(name);
		
		return functionBlock;
	}
	
	/**
	 * Executes this FunctionBlock and returns the output as a String
	 * @return Output of the Function
	 */
	public String executeMethod(){		
		return new String("DEBUG-OUTPUT");
	}
	
	/**
	 * Nest another Node object within this FunctionBlock
	 * @param node to nest
	 */
	public void nest(Node node){
		Pane nestSPace = (Pane) this.lookup("#nest_space");
		((Label) this.lookup("#label_function_name")).setText("Higher order function");
		nestSPace.getChildren().add(node);
	}
	
	/**
	 * Private method to initialize the argument fields for this function block.
	 * All arguments will be stored as Strings.
	 * @param numberOfArguments
	 */
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
