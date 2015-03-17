package nl.utwente.group10.ui.gestures;

import javafx.event.EventType;
import javafx.scene.Node;
import nl.utwente.cs.caes.tactile.event.TactilePaneEvent;

public class UIEvent extends TactilePaneEvent {

	//TODO merge with CustomGestureEvent
	
	
	public static final EventType<UIEvent> TAP = new EventType<>(ANY, "TAP");
	public static final EventType<UIEvent> TAP_HOLD = new EventType<>(ANY, "TAP_HOLD");
	
	public UIEvent(EventType<TactilePaneEvent> eventType, Node target, Node otherNode){
		super(eventType, target, otherNode);
	}
	
}
