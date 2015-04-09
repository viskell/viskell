package nl.utwente.group10.ui.gestures;

import java.util.HashMap;
import java.util.Map;

import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.Connection;
import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.ConnectionLine;
import nl.utwente.group10.ui.components.InputAnchor;
import nl.utwente.group10.ui.components.OutputAnchor;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

public class OutputAnchorHandler implements EventHandler<InputEvent> {

	public static final Integer MOUSE_ID = 0;

	private CustomUIPane cpane;
	private OutputAnchor outputAnchor;

	private Map<Integer, ConnectionLine> lines;

	public OutputAnchorHandler(OutputAnchor outputAnchor, CustomUIPane cpane) {
		this.cpane = cpane;
		this.outputAnchor = outputAnchor;
		lines = new HashMap<Integer, ConnectionLine>();
		outputAnchor.addEventFilter(MouseEvent.ANY, this);
		outputAnchor.addEventFilter(TouchEvent.ANY, this);
	}

	@Override
	public void handle(InputEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent mEvent = ((MouseEvent) event);
			if (mEvent.getEventType().equals(MouseEvent.DRAG_DETECTED)) {
				// Create connection
				createConnection(MOUSE_ID);
			} else if (mEvent.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
				// Move in progress visual-only connection (follow cursor)
				if (lines.get(MOUSE_ID) != null) {
					updateLine(MOUSE_ID, mEvent.getSceneX(), mEvent.getSceneY());
				}
			} else if (mEvent.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
				// Remove visual-only line
				if (lines.get(MOUSE_ID) != null) {
					finalizeLine(MOUSE_ID);
				}
			}
		} else if (event instanceof TouchEvent) {
			TouchEvent tEvent = ((TouchEvent) event);
			int touchID = tEvent.getTouchPoint().getId();
			if (tEvent.getEventType().equals(TouchEvent.TOUCH_PRESSED)) {
				createConnection(touchID);
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
		event.consume();
	}

	private void createConnection(int id) {
		Point2D point = outputAnchor.localToScene(outputAnchor.getCenterX(),
				outputAnchor.getCenterY());
		createLine(id, point.getX(), point.getY());
		outputAnchor.startFullDrag();
	}

	private void createLine(int id, double x, double y) {
		ConnectionLine line = new ConnectionLine();
		cpane.getChildren().add(line);
		line.setStartPosition(x, y);
		line.setEndPosition(x, y);
		lines.put(id, line);
	}

	private void updateLine(int id, double x, double y) {
		lines.get(id).setEndPosition(x, y);
	}

	private void finalizeLine(int id) {
		cpane.getChildren().remove(lines.get(id));
		lines.remove(id);
	}
}
