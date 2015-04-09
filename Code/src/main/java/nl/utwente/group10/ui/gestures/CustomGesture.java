package nl.utwente.group10.ui.gestures;

import java.util.Date;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class CustomGesture extends AbstractGesture implements EventHandler<MouseEvent> {
	private long startTime;
	private boolean draggedWhilePressed;
	private GestureCallBack callBack;

	public CustomGesture(GestureCallBack callBack, Node latchTo) {
		super(latchTo);
		this.callBack = callBack;
		System.out.println("Created!");
	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
			startTime = System.currentTimeMillis();
		}
		if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
			if ((System.currentTimeMillis() - startTime) < 500) {

				callBack.handleCustomEvent(new UIEvent(UIEvent.TAP));
			} else {
				if(!draggedWhilePressed){
					callBack.handleCustomEvent(new UIEvent(UIEvent.TAP_HOLD));
				} else {
					callBack.handleCustomEvent(new UIEvent(UIEvent.DRAG));
					draggedWhilePressed = false;
				}
			}
		}
		if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)){
			draggedWhilePressed = true;
		}
	}

	@Override
	protected void latch() {
		latchTo.addEventHandler(MouseEvent.ANY, this);
	}

}
