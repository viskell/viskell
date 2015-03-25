package nl.utwente.group10.ui.components;

import java.io.IOException;

import nl.utwente.cs.caes.tactile.fxml.TactileBuilderFactory;
import nl.utwente.group10.ui.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.shape.Circle;

/**
 * Represent an Anchor point on either a Block or a Line
 * Integers are currently the only supported data type.
 * 
 * Other data types will be supported in the future
 */
public class ConnectionAnchor extends Circle {
	
	/** Indication of what dataType this Anchor supports.*/
	public String dataType = "Int";
	/** Used to determine whether this Anchor serves output or accepts Input.*/
	private boolean isOutput;
	
	public static ConnectionAnchor newInstance(boolean isOutput) throws IOException {
		ConnectionAnchor connectionAnchor = FXMLLoader.load(Main.class.getResource("/ui/ConnectionAnchor.fxml"), null, new TactileBuilderFactory());			
		
		return connectionAnchor;
	}
	
	
}