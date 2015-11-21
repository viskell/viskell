package nl.utwente.ewi.caes.tactilefx;

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
    /**
     * Defines whether an Event is processed at the filter stage or the handler stage.
     */
    public enum EventProcessingMode {
        /**
         * Represents processing events at the handler stage
         */
        HANDLER, 
        
        /**
         * Represents processing events at the filter stage.
         */
        FILTER
    }

    public static final int NULL_ID = -1;
    public static final int MOUSE_ID = -2;
    
    final Node node;         // Node that is being dragged
    /* Whether this node will go to the foreground when the user starts a drag gesture with it. */
    boolean goToForegroundOnContact; //
    double localOffsetX, localOffsetY;  // The x,y position of the Event in the Node
    int touchId;            // The id of the finger/cursor that is currently dragging the Node
    
    private final EventHandler<TouchEvent> touchHandler;
    private final EventHandler<MouseEvent> mouseHandler;
    
    public DragContext(Node draggable, DragContext.EventProcessingMode dragProcessingMode) {
        this.node = draggable;
        this.goToForegroundOnContact = true;
        touchId = -1;
        
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
        
        if (dragProcessingMode == DragContext.EventProcessingMode.FILTER) {
            draggable.addEventFilter(TouchEvent.ANY, touchHandler);
            draggable.addEventFilter(MouseEvent.ANY, mouseHandler);
        } else {
            draggable.addEventHandler(TouchEvent.ANY, touchHandler);
            draggable.addEventHandler(MouseEvent.ANY, mouseHandler);
        }
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
    
    public void removeDragEventHandlers() {
        node.removeEventFilter(TouchEvent.ANY, touchHandler);
        node.removeEventFilter(MouseEvent.ANY, mouseHandler);
        node.removeEventHandler(TouchEvent.ANY, touchHandler);
        node.removeEventHandler(MouseEvent.ANY, mouseHandler);
    }
    
    /**
     * The Node that is being dragged
     */
    public Node getDraggable() {
        return this.node;
    }
    
    /**
     * The id of the TouchPoint that is responsible for dragging the Node.
     * Returns NULL_ID if the Node is not being dragged, or MOUSE_ID if the
     * Node is dragged by a mouse cursor.
     */
    public int getTouchId() {
        return touchId;
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
