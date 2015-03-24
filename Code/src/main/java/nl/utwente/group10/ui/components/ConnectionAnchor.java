package nl.utwente.group10.ui.components;

import java.io.IOException;

import nl.utwente.cs.caes.tactile.fxml.TactileBuilderFactory;
import nl.utwente.group10.ui.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.shape.Circle;

public class ConnectionAnchor extends Circle {
	//TODO add Connection adding code
	
	public static ConnectionAnchor newInstance() throws IOException {
		ConnectionAnchor connectionAnchor = FXMLLoader.load(Main.class.getResource("/ui/ConnectionAnchor.fxml"), null, new TactileBuilderFactory());			
		
		return connectionAnchor;
	}
}