package nl.utwente.group10.ui.gestures;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class CustomGesture implements EventHandler<MouseEvent> {

	private GestureCallBack callBack;
	private boolean isTapHold = false;
	private Timer tt = new Timer(); 

	public CustomGesture(GestureCallBack callBack, Node latchTo) {
		this.callBack = callBack;
		latchTo.addEventHandler(MouseEvent.ANY, this);
	}

	@Override
	public void handle(MouseEvent event) {
		if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)){
			tt.schedule(new TimeOutTask(), 500);	
		}
		if(event.getEventType().equals(MouseEvent.MOUSE_RELEASED)){
			if(!isTapHold){
				tt.cancel();
				tt.purge();
				callBack.handleCustomEvent(new CustomGestureEvent(CustomGestureEvent.TAP));
			}
			isTapHold = false;
		}
	}
	
	private class TimeOutTask extends TimerTask {

		@Override
		public void run() {
			callBack.handleCustomEvent(new CustomGestureEvent(CustomGestureEvent.TAP_HOLD));
			isTapHold = true;
		}
		
	}

}
