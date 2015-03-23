package nl.utwente.group10.ui.components;

import javafx.scene.layout.StackPane;

/**
 * Base UI Component that other visual elements will extend from.
 * If common functionality is found it should be refactored to here.
 */
public class Block extends StackPane {

	/** Selected state of this Block*/
	private boolean isSelected = false;
	
	/**
	 * Set the selected state of this Block
	 * @param Selected False/True
	 */
		public void setSelected(boolean bool){
			//TODO If another object is selected then deselect it first!!
			isSelected = bool;
		}
}
