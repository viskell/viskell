package nl.utwente.group10.ui.components;


import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.gestures.GestureCallBack;
import nl.utwente.group10.ui.gestures.UIEvent;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Base UI Component that other visual elements will extend from.
 * If common functionality is found it should be refactored to here.
 */

public class Block extends StackPane implements Initializable, GestureCallBack {
	
	/** Selected state of this Block*/
	private boolean isSelected = false;
	
	/** The fxmlLoader responsible for loading the fxml of this Block.*/
	private FXMLLoader fxmlLoader;
	
	private CustomUIPane cup;
	
	public Block(String blockName, CustomUIPane pane) throws IOException {
		fxmlLoader = new FXMLLoader(getClass().getResource("/ui/"+blockName+".fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		
		cup = pane;
	}
	
	/**
	 * Returns the FXMLLoader use by this Block.
	 * @return fxmlLoader;
	 */
	public FXMLLoader getLoader(){
		return fxmlLoader;
	}
	
	/**
	 * Set the selected boolean state of this Block
	 * @param selectedState
	 */
	public void setSelected(boolean selectedState) {
		//TODO If another object is selected then deselect it first!!
		isSelected = selectedState;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	@Override
	public void handleCustomEvent(UIEvent event) {
		EventType eventType = event
				.getEventType();
		if (eventType.equals(UIEvent.TAP)) {
			for(Node n : cup.getChildren()){
				if(n instanceof Block){
					if(((Block) n).isSelected){
						((Block) n).setSelected(false);
					}
				}
			}
			this.setSelected(true);
			System.out.println("Block is selected");
		} else if (eventType.equals(UIEvent.TAP_HOLD)) {
			//TODO: open the quick-menu
		}
	}
}
