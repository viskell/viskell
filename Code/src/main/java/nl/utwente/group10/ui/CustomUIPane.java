package nl.utwente.group10.ui;

import javafx.event.EventType;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.group10.ui.gestures.CustomGesture;
import nl.utwente.group10.ui.gestures.UIEvent;
import nl.utwente.group10.ui.gestures.GestureCallBack;

public class CustomUIPane extends TactilePane implements GestureCallBack {

	public CustomUIPane() {
		CustomGesture cg = new CustomGesture(this, this);
	}

	@Override
	public void handleCustomEvent(UIEvent event) {
		EventType<UIEvent> eventType = (EventType<UIEvent>) event
				.getEventType();
	
	}
}
