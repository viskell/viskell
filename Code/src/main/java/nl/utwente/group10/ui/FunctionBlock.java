package nl.utwente.group10.ui;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

public class FunctionBlock extends StackPane{
	
	//The arguments this FunctionBlock holds
	private String[] arguments;
	private String functionName;
	private boolean isSelected = false;
	
	
	public static FunctionBlock newInstance(int numberOfArguments) throws IOException{
		//TODO Prettier resource loading
		FunctionBlock functionBlock = (FunctionBlock) FXMLLoader.load(Main.class.getResource("FunctionBlock.fxml"), null, new FunctionBlockBuilderFactory());
		functionBlock.initializeArguments(numberOfArguments);
		
		return functionBlock;
	}
	
	//TODO To be implemented method that will parse code into Haskell interface
	public void executeMethod(){
		
	}
	
	//Method to initialize the argument fields for this function block.
	//All arguments will be stored as Strings.
	private void initializeArguments(int numberOfArguments){
		arguments = new String[numberOfArguments];
	}
	
	//Sets the String value for a specified Argument
	public void setArgument(int i,String arg){
		arguments[i] = arg;
	}
	
	//Selects or deselects this FunctionBlock
	public void setSelected(boolean bool){
		//If another object is selected then deselect it first!!
		isSelected = bool;
	}
	
	//Sets the functionName
	public void setName(String name){
		functionName = name;
		Label label = ((Label)this.lookup("#label_function_name"));
		label.setText(functionName);
	}
}
