package nl.utwente.group10.ui.handlers;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import nl.utwente.group10.ui.components.ConnectionAnchor;

public class AnchorHandler implements EventHandler<InputEvent> {

	private ConnectionCreationManager manager;
	private ConnectionAnchor anchor;

	public AnchorHandler(ConnectionCreationManager manager,
			ConnectionAnchor anchor) {
		this.manager = manager;
		this.anchor = anchor;

		anchor.addEventFilter(MouseEvent.MOUSE_PRESSED, this);
		anchor.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
		anchor.addEventFilter(MouseEvent.MOUSE_RELEASED, this);

		anchor.addEventFilter(TouchEvent.TOUCH_PRESSED, this);
		anchor.addEventFilter(TouchEvent.TOUCH_MOVED, this);
		anchor.addEventFilter(TouchEvent.TOUCH_RELEASED, this);
	}

	@Override
	public void handle(InputEvent event) {
		Node pickResult = null;
		int inputId = -1;
		double x = 0;
		double y = 0;

		if (event instanceof MouseEvent
				&& !((MouseEvent) event).isSynthesized()) {
			MouseEvent mEvent = ((MouseEvent) event);
			pickResult = mEvent.getPickResult().getIntersectedNode();
			inputId = ConnectionCreationManager.MOUSE_ID;
			x = mEvent.getSceneX();
			y = mEvent.getSceneY();
		} else if (event instanceof TouchEvent) {
			TouchPoint tp = ((TouchEvent) event).getTouchPoint();
			pickResult = tp.getPickResult().getIntersectedNode();
			inputId = tp.getId();
			x = tp.getSceneX();
			y = tp.getSceneY();
		}

		if (pickResult != null && inputId >= 0) {
			if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)
					|| event.getEventType().equals(TouchEvent.TOUCH_PRESSED)) {
				inputPressed(inputId);
			} else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)
					|| event.getEventType().equals(TouchEvent.TOUCH_MOVED)) {
				inputMoved(inputId,x,y);
			} else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)
					|| event.getEventType().equals(TouchEvent.TOUCH_RELEASED)) {
				inputReleased(inputId,pickResult);
			}
		}
		event.consume();
	}
	
	private void inputPressed(int inputId){
		if (anchor.getConnection().isPresent() && !anchor.canConnect()) {
			manager.editConnection(inputId, anchor);
		} else {
			manager.createConnectionWith(inputId, anchor);
		}
	}
	
	private void inputMoved(int inputId, double x, double y){
		manager.updateLine(inputId, x, y);
	}
	
	private void inputReleased(int inputId, Node pickResult){
		if (pickResult instanceof ConnectionAnchor) {
			manager.finishConnection(
					inputId,
					(ConnectionAnchor) pickResult);
		} else {
			manager.removeConnection(inputId);
		}
	}
}
