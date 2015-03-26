package nl.utwente.group10.ui.components;

import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Base UI Component that other visual elements will extend from.
 * If common functionality is found it should be refactored to here.
 */
public class Block extends StackPane implements Initializable {

	/** Selected state of this Block*/
	private boolean isSelected = false;
	
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
}
