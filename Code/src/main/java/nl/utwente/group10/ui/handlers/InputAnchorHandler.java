package nl.utwente.group10.ui.handlers;

import javafx.event.EventHandler;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import nl.utwente.group10.ui.components.InputAnchor;

public class InputAnchorHandler implements EventHandler<InputEvent> {

	private ConnectionCreationManager manager;
	private InputAnchor inputAnchor;

	public InputAnchorHandler(ConnectionCreationManager manager,
			InputAnchor inputAnchor) {
		this.manager = manager;
		this.inputAnchor = inputAnchor;

		inputAnchor.addEventFilter(MouseEvent.DRAG_DETECTED, this);
		inputAnchor.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
		inputAnchor.addEventFilter(MouseEvent.MOUSE_RELEASED, this);
		inputAnchor.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, this);
		inputAnchor.addEventFilter(TouchEvent.ANY, this);
	}

	@Override
	public void handle(InputEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent mEvent = (MouseEvent) event;
			if (mEvent.getEventType()
					.equals(MouseDragEvent.MOUSE_DRAG_RELEASED)) {
				// Finalize connection
				manager.finishConnection(ConnectionCreationManager.MOUSE_ID,
						inputAnchor);
			} else if (mEvent.getEventType().equals(MouseEvent.DRAG_DETECTED)) {
				if (inputAnchor.getConnection().isPresent()) {
					// Edit (move) existing connection
					manager.editConnection(ConnectionCreationManager.MOUSE_ID,
							inputAnchor);
				} else {
					// Create new connection
					manager.createConnectionWith(
							ConnectionCreationManager.MOUSE_ID, inputAnchor);
				}
			} else if (mEvent.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
				// Move in progress visual-only connection (follow cursor)
				manager.updateLine(ConnectionCreationManager.MOUSE_ID,
						mEvent.getSceneX(), mEvent.getSceneY());
			} else if (mEvent.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
				// Remove visual-only connection
				manager.finishConnection(ConnectionCreationManager.MOUSE_ID);
			}
		}

		// TODO TouchDragEvent does not exist. Test if touch fires synthesized
		// MouseDragEvents, else find workaround.

		event.consume();
	}
}
