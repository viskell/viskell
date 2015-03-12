package nl.utwente.group10.ui;
import javafx.event.Event;
import javafx.event.EventType;

public class CustomGestureEvent extends Event {
	
	public static final EventType<CustomGestureEvent> ANY = new EventType<>(
			Event.ANY, "ANY");
	public static final EventType<CustomGestureEvent> TAP = new EventType<>(ANY, "TAP");
	public static final EventType<CustomGestureEvent> TAP_HOLD = new EventType<>(ANY, "TAP_HOLD");
	
	public CustomGestureEvent(EventType<CustomGestureEvent> eventType) {
		super(eventType);
	}
}
