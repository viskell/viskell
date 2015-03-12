package nl.utwente.group10.ui.gestures;

import java.sql.Time;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class CustomGesture implements EventHandler<MouseEvent> {

	private GestureCallBack callBack;
	//Time in milliseconds: 1000 ms = 1 second
	private long startTime; 
	private long endTime;
	
	public CustomGesture(GestureCallBack callBack, Node latchTo) {
		this.callBack = callBack;
		latchTo.addEventHandler(MouseEvent.ANY, this);
	}

	@Override
	public void handle(MouseEvent event) {
		if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)){
			startTime = System.currentTimeMillis();
		}
		if(event.getEventType().equals(MouseEvent.MOUSE_RELEASED)){
			if(System.currentTimeMillis() - startTime > 500){
				callBack.handleCustomEvent(new CustomGestureEvent(CustomGestureEvent.TAP_HOLD));
			} else {
				callBack.handleCustomEvent(new CustomGestureEvent(CustomGestureEvent.TAP));
			}
		}
	}

}
