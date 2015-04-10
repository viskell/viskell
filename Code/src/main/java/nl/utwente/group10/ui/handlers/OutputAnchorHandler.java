package nl.utwente.group10.ui.handlers;

import javafx.event.EventHandler;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import nl.utwente.group10.ui.components.OutputAnchor;

public class OutputAnchorHandler implements EventHandler<InputEvent> {
	private ConnectionCreationManager manager;
	private OutputAnchor outputAnchor;

	public OutputAnchorHandler(ConnectionCreationManager manager,
			OutputAnchor outputAnchor) {
		this.manager = manager;
		this.outputAnchor = outputAnchor;

		outputAnchor.addEventFilter(MouseEvent.DRAG_DETECTED, this);
		outputAnchor.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
		outputAnchor.addEventFilter(MouseEvent.MOUSE_RELEASED, this);
		outputAnchor.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, this);
		outputAnchor.addEventFilter(TouchEvent.ANY, this);
	}

	@Override
	public void handle(InputEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent mEvent = ((MouseEvent) event);
			if (mEvent.getEventType()
					.equals(MouseDragEvent.MOUSE_DRAG_RELEASED)) {
				// Finalize connection
				manager.finishConnection(ConnectionCreationManager.MOUSE_ID,
						outputAnchor);
			} else if (mEvent.getEventType().equals(MouseEvent.DRAG_DETECTED)) {
				// Create connection
				manager.createConnectionWith(
						ConnectionCreationManager.MOUSE_ID, outputAnchor);
			} else if (mEvent.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
				// Move in progress visual-only connection (follow cursor)
				manager.updateLine(ConnectionCreationManager.MOUSE_ID,
						mEvent.getSceneX(), mEvent.getSceneY());
			} else if (mEvent.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
				// Remove visual-only line
				manager.finishConnection(ConnectionCreationManager.MOUSE_ID);
			}
		}

		event.consume();
	}
}
