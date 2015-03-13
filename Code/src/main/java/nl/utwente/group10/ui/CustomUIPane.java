package nl.utwente.group10.ui;

import javafx.event.EventType;
import nl.utwente.cs.caes.tactile.control.TactilePane;
import nl.utwente.group10.ui.gestures.CustomGesture;
import nl.utwente.group10.ui.gestures.CustomGestureEvent;
import nl.utwente.group10.ui.gestures.GestureCallBack;

public class CustomUIPane extends TactilePane implements GestureCallBack {

	public CustomUIPane() {
		CustomGesture cg = new CustomGesture(this, this);
	}

	@Override
	public void handleCustomEvent(CustomGestureEvent event) {
		EventType<CustomGestureEvent> eventType = (EventType<CustomGestureEvent>) event.getEventType();
		if(eventType.equals(CustomGestureEvent.TAP)){
			System.out.println("CustomUIPane -> CustomGestureEvent.TAP");
			//TODO:select element if this element has the property to be selected
		} else if(eventType.equals(CustomGestureEvent.TAP_HOLD)){
			System.out.println("CustomUIPane -> CustomGestureEvent.TAP_HOLD");
			//TODO: open the quick-menu of an element if this is possible
		} else if(eventType.equals(CustomGestureEvent.ANY)){
			System.out.println("CustomUIPane -> CustomGestureEvent.ANY");
		}
	}
}
