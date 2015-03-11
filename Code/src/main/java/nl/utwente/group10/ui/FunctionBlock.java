package nl.utwente.group10.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class FunctionBlock extends StackPane{
	
	
	public static FunctionBlock newInstance() throws IOException{
		//TODO Prettier resource loading
		FunctionBlock fuctionBlock = (FunctionBlock) FXMLLoader.load(Main.class.getResource("FunctionBlock.fxml"), null, new FunctionBlockBuilderFactory());
		return fuctionBlock;
	}
	
	
	public void nest(Node node){
		Pane nestSPace = (Pane) this.lookup("#nest_space");
		((Label) this.lookup("#label_function_name")).setText("Trolololol");
		nestSPace.getChildren().add(node);
	}
}
