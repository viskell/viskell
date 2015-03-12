package nl.utwente.group10.ui.gestures;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class CustomGesture implements EventHandler<MouseEvent> {

	private GestureCallBack callBack;

	public CustomGesture(GestureCallBack callBack, Node latchTo) {
		this.callBack = callBack;
		latchTo.addEventHandler(MouseEvent.ANY, this);
	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
			callBack.handleCustomEvent(new CustomGestureEvent(
					CustomGestureEvent.ANY));
		}else{
			System.out.println(event.getEventType());
		}
	}

}
