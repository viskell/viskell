package nl.utwente.group10.ui;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class FunctionBlock extends StackPane{
	public static FunctionBlock newInstance() throws IOException{
		//TODO Prettier resource loading
		FunctionBlock fuctionBlock = (FunctionBlock) FXMLLoader.load(Main.class.getResource("FunctionBlock.fxml"), null, new FunctionBlockBuilderFactory());
		return fuctionBlock;
	}
	
	
	public void sayHI(){
		System.out.println("HI!!!");
	}
	
}
