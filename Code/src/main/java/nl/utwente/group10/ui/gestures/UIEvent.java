package nl.utwente.group10.ui.gestures;


import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import nl.utwente.ewi.caes.tactilefx.event.TactilePaneEvent;

public class UIEvent extends Event {	
	
	public static final EventType<UIEvent> ANY = new EventType<>(Event.ANY, "ANY");
	public static final EventType<UIEvent> TAP = new EventType<>(ANY, "TAP");
	public static final EventType<UIEvent> TAP_HOLD = new EventType<>(ANY, "TAP_HOLD");
	public static final EventType<UIEvent> DRAG = new EventType<>(ANY, "DRAG");
	private EventType<UIEvent> eventType;
	
	public UIEvent(EventType<UIEvent> eventType){
		super(eventType);
		this.eventType = eventType;
	}
	
	@Override
	public String toString() {
		String result;
		if (eventType.equals(ANY)) {
			result = "CustomGestureEvent type = ANY";
		} else if (eventType.equals(TAP)) {
			result = "CustomGestureEvent type = TAP";
		} else if (eventType.equals(TAP_HOLD)) {
			result = "CustomGestureEvent type = TAP_HOLD";
		} else {
			result = "Nog niet opgenomen CustomGestureEvent";
		}
		return result;
	}
}
