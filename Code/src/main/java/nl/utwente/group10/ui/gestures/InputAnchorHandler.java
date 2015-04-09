package nl.utwente.group10.ui.gestures;

import java.util.HashMap;
import java.util.Map;

import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.ConnectionLine;
import nl.utwente.group10.ui.components.InputAnchor;
import nl.utwente.group10.ui.components.OutputAnchor;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class InputAnchorHandler implements EventHandler<InputEvent> {

	public static final Integer MOUSE_ID = 0;
	private CustomUIPane cpane;
	private InputAnchor inputAnchor;
	private Map<Integer, ConnectionLine> lines;

	public InputAnchorHandler(InputAnchor inputAnchor, CustomUIPane cpane) {
		this.cpane = cpane;
		this.inputAnchor = inputAnchor;
		lines = new HashMap<Integer, ConnectionLine>();
		inputAnchor.addEventFilter(MouseEvent.ANY, this);
		inputAnchor.addEventFilter(TouchEvent.ANY, this);
	}

	@Override
	public void handle(InputEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent mEvent = (MouseEvent) event;
			if (event instanceof MouseDragEvent) {
				MouseDragEvent mdEvent = ((MouseDragEvent) event);
				if (mdEvent.getEventType().equals(
						MouseDragEvent.MOUSE_DRAG_RELEASED)) {
					if (mdEvent.getGestureSource() instanceof OutputAnchor) {
						// Finalize connection
						createConnection(mdEvent);
					}
				}
			} else if (mEvent.getEventType().equals(MouseEvent.DRAG_DETECTED)) {
				if (inputAnchor.getConnection().isPresent()) {
					// Edit (move) existing connection
					moveConnection(MOUSE_ID);
				}
			} else if (mEvent.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
				// Move in progress visual-only connection (follow cursor)
				if (lines.get(MOUSE_ID) != null) {
					updateLine(MOUSE_ID, mEvent.getSceneX(), mEvent.getSceneY());
				}
			} else if (mEvent.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
				// Remove visual-only connection
				if (lines.get(MOUSE_ID) != null) {
					finalizeLine(MOUSE_ID);
				}
			}
		} else if (event instanceof TouchEvent) {
			TouchEvent tEvent = ((TouchEvent) event);
			int touchID = tEvent.getTouchPoint().getId();
			if (tEvent.getEventType().equals(TouchEvent.TOUCH_PRESSED)) {
				moveConnection(touchID);
			} else if (tEvent.getEventType().equals(TouchEvent.TOUCH_MOVED)) {
				if (lines.get(touchID) != null) {
					updateLine(touchID, tEvent.getTouchPoint().getSceneX(),
							tEvent.getTouchPoint().getSceneY());
				}
			} else if (tEvent.getEventType().equals(TouchEvent.TOUCH_RELEASED)) {
				if (lines.get(touchID) != null) {
					finalizeLine(touchID);
				}
			}
		}

		// TODO TouchDragEvent does not exist. Test if touch fires synthesized
		// MouseDragEvents, else find workaround.

		event.consume();
	}

	private void createLine(int id, double x, double y) {
		ConnectionLine line = new ConnectionLine();
		cpane.getChildren().add(line);
		line.setMouseTransparent(true);
		line.setStartPosition(x, y);
		line.setEndPosition(x, y);
		lines.put(id, line);
	}

	private void updateLine(int id, double x, double y) {
		lines.get(id).setEndPosition(x, y);
	}

	private void finalizeLine(int id) {
		lines.get(id).setMouseTransparent(false);
		cpane.getChildren().remove(lines.get(id));
		cpane.invalidate();
		lines.remove(id);
	}

	private void removeConnection() {
		cpane.getChildren().remove(inputAnchor.getConnection().get());
	}

	private void createConnection(MouseDragEvent mdEvent) {
		inputAnchor.createConnectionFrom((OutputAnchor) mdEvent
				.getGestureSource());
		cpane.invalidate();
	}

	private void moveConnection(int id) {
		OutputAnchor outputAnchor = inputAnchor.getConnection().get()
				.getOutputAnchor();
		outputAnchor.startFullDrag();
		Point2D point = outputAnchor.localToScene(outputAnchor.getCenterX(),
				outputAnchor.getCenterY());
		createLine(id, point.getX(), point.getY());
		removeConnection();
	}
}
