package nl.utwente.group10.ui.handlers;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;

/**
 * Handler class that reacts to user inputs on ConnectionAnchors to be able to
 * create edit and drag Connections.
 */
public class AnchorHandler implements EventHandler<InputEvent> {
    /** The ConnectionCreationManager to which this AnchorHandler belongs. */
	private ConnectionCreationManager manager;
	/** The ConnectionAnchor to which this AnchorHandler belongs. */
	private ConnectionAnchor anchor;

    /**
     * Constructs a new AnchorHandler
     * 
     * @param manager
     *            The ConnectionCreationManager to which this AnchorHandler
     *            belongs.
     * @param anchor
     *            The ConnectionAnchor to which this AnchorHandler belongs.
     */
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
		int inputId = ConnectionCreationManager.INPUT_ID_NONE;
		double x = 0;
		double y = 0;
		
		// Extract input information for either mouse or touch.
		if (event instanceof MouseEvent
				&& !((MouseEvent) event).isSynthesized()) {
			MouseEvent mEvent = ((MouseEvent) event);
			pickResult = mEvent.getPickResult().getIntersectedNode().getParent();
			inputId = ConnectionCreationManager.INPUT_ID_MOUSE;
			x = mEvent.getSceneX();
			y = mEvent.getSceneY();
		} else if (event instanceof TouchEvent) {
			TouchPoint tp = ((TouchEvent) event).getTouchPoint();
			pickResult = tp.getPickResult().getIntersectedNode().getParent();
			inputId = tp.getId();
			x = tp.getSceneX();
			y = tp.getSceneY();
		}

		// Use the input information to call the appropriate method.
		if (pickResult != null && (inputId == ConnectionCreationManager.INPUT_ID_MOUSE || inputId > 0)) {
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
	
    /**
     * Method to indicate that the Anchor is pressed with the given inputId.
     * 
     * @param inputId
     *            The id associated with the input that triggered this action.
     */
	private void inputPressed(int inputId){
		if (anchor.getPrimaryConnection().isPresent() && !anchor.canAddConnection()) {
			manager.editConnection(inputId, anchor);
		} else {
			manager.buildConnectionWith(inputId, anchor);
		}
	}
	
    /**
     * Method to indicate that the input moved.
     * 
     * @param inputId
     *            The id associated with the input that triggered this action.
     * @param x
     *            The sceneX coordinate of the input.
     * @param y
     *            The sceneY coordinate of the input.
     */
	private void inputMoved(int inputId, double x, double y){
		manager.updateLine(inputId, x, y);
	}
	
	/**
     * Method to indicate that the input was released.
     * 
     * @param inputId
     *            The id associated with the input that triggered this action.
     * @param pickResult
     *            The PickResult done at the position where the input was
     *            released.
     */
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
