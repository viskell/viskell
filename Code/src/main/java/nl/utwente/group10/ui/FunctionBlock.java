package nl.utwente.group10.ui;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class FunctionBlock extends StackPane{
	
	//The arguments this FunctionBlock holds
	private String[] arguments;
	
	public static FunctionBlock newInstance(int numberOfArguments) throws IOException{
		//TODO Prettier resource loading
		FunctionBlock functionBlock = (FunctionBlock) FXMLLoader.load(Main.class.getResource("FunctionBlock.fxml"), null, new FunctionBlockBuilderFactory());
		functionBlock.initializeArguments(2);
		
		return functionBlock;
	}
	
	//To be implemented method that will parse code into haskell interface
	public void executeMethod(){
		
	}
	
	//Method to initialize the argument fields for this function block.
	//All arguments will be stored as Strings.
	private void initializeArguments(int numberOfArguments){
		arguments = new String[numberOfArguments];
	}
}
