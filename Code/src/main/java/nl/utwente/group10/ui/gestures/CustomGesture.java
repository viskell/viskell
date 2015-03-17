package nl.utwente.group10.ui.gestures;

import java.sql.Time;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import nl.utwente.group10.ui.CustomUIPane;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class CustomGesture implements EventHandler<MouseEvent> {

	private GestureCallBack callBack;
	private long startTime;
	private Date date = new Date();

	public CustomGesture(GestureCallBack callBack, Node latchTo) {
		this.callBack = callBack;
		latchTo.addEventHandler(MouseEvent.ANY, this);
	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
			startTime = System.currentTimeMillis();
		}
		if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
			if ((System.currentTimeMillis() - startTime) < 500) {

				callBack.handleCustomEvent(new CustomGestureEvent(
						CustomGestureEvent.TAP));
				System.out.println("CustomGesture -> CustomGestureEvent.TAP");
			} else {
				callBack.handleCustomEvent(new CustomGestureEvent(
						CustomGestureEvent.TAP_HOLD));
				System.out
						.println("CustomGesture -> CustomGestureEvent.TAP_HOLD");
			}
		}
	}

}
