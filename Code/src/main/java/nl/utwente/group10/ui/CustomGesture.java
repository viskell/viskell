package nl.utwente.group10.ui;

import java.sql.Time;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class CustomGesture implements EventHandler<MouseEvent> {

	private GestureCallBack callBack;
	//Tijd in miliseconden --> 1000 miliseconde = 1 seconde.
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
		//if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
		//	callBack.handleCustomEvent(new CustomGestureEvent(
		//			CustomGestureEvent.ANY));
		//}
		else{
			System.out.println(event.getEventType());
		}
	}

}
