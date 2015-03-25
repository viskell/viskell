package nl.utwente.group10.ui.components;

import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.gestures.GestureCallBack;
import nl.utwente.group10.ui.gestures.UIEvent;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Base UI Component that other visual elements will extend from.
 * If common functionality is found it should be refactored to here.
 */
public class Block extends StackPane implements GestureCallBack {

	/** Selected state of this Block*/
	private boolean isSelected = false;
	
	protected static CustomUIPane cup;
	
	/**
	 * Set the selected boolean state of this Block
	 * @param selectedState
	 */
	public void setSelected(boolean selectedState) {
		//TODO If another object is selected then deselect it first!!
		isSelected = selectedState;
	}
	
	@Override
	public void handleCustomEvent(UIEvent event) {
		EventType<UIEvent> eventType = (EventType<UIEvent>) event
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
