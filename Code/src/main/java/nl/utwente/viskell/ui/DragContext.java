package nl.utwente.viskell.ui;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

/**
 * Helper class used for event handling of dragging a Node.
 */
public class DragContext {

    /** The id of the finger/cursor that is currently dragging the Node */
    private int touchId;

    /** Touch ID representing the mouse cursor. */
    private static final int MOUSE_ID = -1;
    /** An unused touch ID. */
    private static final int NULL_ID = Integer.MIN_VALUE;
    
    /** The Node that can be dragged. */
    final Node node;         // Node that is being dragged

    /** Whether this node will go to the foreground when the user starts a drag gesture with it. */
    boolean goToForegroundOnContact;
    
    /** The x,y position in the Node where the dragging started. */
    private double localOffsetX, localOffsetY;
    
    /** reference to internal touch event handler */
    private final EventHandler<TouchEvent> touchHandler;

    /** reference to internal mouse event handler */
    private final EventHandler<MouseEvent> mouseHandler;
    
    /**
     * Creates a DragContext keeping track of touch events, so that a Node is made draggable.
     * @param draggable the Node that is to be made draggable.
     */
    public DragContext(Node draggable) {
        this.node = draggable;
        this.goToForegroundOnContact = true;
        touchId = NULL_ID;
        
        touchHandler = event -> {
            EventType<TouchEvent> type = event.getEventType();
            
            if (type == TouchEvent.TOUCH_PRESSED) {
                if (this.touchId == DragContext.NULL_ID) {
                    this.touchId = event.getTouchPoint().getId();
                    this.handleTouchPressed(event.getTouchPoint().getX(), event.getTouchPoint().getY());
                    event.consume();
                }
            } else if (type == TouchEvent.TOUCH_MOVED) {
                if (this.touchId == event.getTouchPoint().getId()) {
                    this.handleTouchMoved(event.getTouchPoint().getSceneX(), event.getTouchPoint().getSceneY());
                    event.consume();
                }
            } else if (type == TouchEvent.TOUCH_RELEASED) {
                if (this.touchId == event.getTouchPoint().getId()) {
                    this.touchId = DragContext.NULL_ID;
                    event.consume();
                }
            }
        };
        
        mouseHandler = event -> {
            if (event.isSynthesized()) {
                event.consume();
                return;
            }
            
            EventType<? extends MouseEvent> type = event.getEventType();
            
            if (type == MouseEvent.MOUSE_PRESSED) {
                if (this.touchId == DragContext.NULL_ID) {
                    this.touchId = DragContext.MOUSE_ID;
                    this.handleTouchPressed(event.getX(), event.getY());
                    event.consume();
                }
            } else if (type == MouseEvent.MOUSE_DRAGGED) {
                
                if (this.touchId == DragContext.MOUSE_ID) {
                    this.handleTouchMoved(event.getSceneX(), event.getSceneY());
                    event.consume();
                }
            } else if (type == MouseEvent.MOUSE_RELEASED) {
                if (this.touchId == DragContext.MOUSE_ID) {
                    this.touchId = DragContext.NULL_ID;
                    event.consume();
                }
            }
        };
        
        draggable.addEventHandler(TouchEvent.ANY, touchHandler);
        draggable.addEventHandler(MouseEvent.ANY, mouseHandler);
    }
    
    private void handleTouchPressed(double localX, double localY) {
        this.localOffsetX = localX;
        this.localOffsetY = localY;

        if (this.goToForegroundOnContact) {
            node.toFront();
        }
    }

    private void handleTouchMoved(double sceneX, double sceneY) {
        Point2D parentPos = this.node.getParent().sceneToLocal(sceneX, sceneY);
        node.relocate(parentPos.getX() - this.localOffsetX, parentPos.getY() - this.localOffsetY);
    }
    
    /** Make the attached Node stop acting on drag actions by removing drag event handlers */
    public void removeDragEventHandlers() {
        node.removeEventHandler(TouchEvent.ANY, touchHandler);
        node.removeEventHandler(MouseEvent.ANY, mouseHandler);
    }
    
    /**
     * The Node that is being dragged
     */
    public Node getDraggable() {
        return this.node;
    }
    
    /** Sets whether the attached node will go to foreground on contact.  */
    public void setGoToForegroundOnContact(boolean goToForegroundOnContact) {
        this.goToForegroundOnContact = goToForegroundOnContact;
    }

    /**
     * Binds the DragContext to a different TouchEvent. This allows a TouchPoint other than
     * the one that started the drag operation to take over the drag gesture.
     * 
     * @throws IllegalArgumentException Thrown when the TouchEvent is not of 
     * type TouchEvent.TOUCH_PRESSED, or when the event's target is not the Node that this
     * DragContext belongs to, or have that Node as ancestor.
     */
    public void bind(TouchEvent event) {
        if (event.getTouchPoint().getId() == touchId) return;
        
        Node target = (Node) event.getTarget();
        while (target.getParent() != this.node) {
            target = target.getParent();
            if (target == null) {
                throw new IllegalArgumentException("TouchEvent's target should be draggable, or have draggable as ancestor");
            }
        }
        if (event.getEventType() != TouchEvent.TOUCH_PRESSED) {
            throw new IllegalArgumentException("TouchEvent should be of type TOUCH_PRESSED");
        }
        
        touchId = event.getTouchPoint().getId();
        this.handleTouchPressed(event.getTouchPoint().getX(), event.getTouchPoint().getY());
    }
    
    @Override
    public String toString() {
        return String.format("DragContext [draggable = %s, ,touchId = %d, localOffsetX = %f, localOffsetY = %f]", node.toString(), touchId, localOffsetX, localOffsetY);
    }
}
