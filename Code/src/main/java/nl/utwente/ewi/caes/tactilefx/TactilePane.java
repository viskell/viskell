package nl.utwente.ewi.caes.tactilefx;

import javafx.beans.DefaultProperty;
import javafx.collections.*;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Region;

/**
 * <p>
 * A Control that allows a user to rearrange the position of its children. Acts
 * like a {@code Pane} in that it only resizes its children to its preferred
 * sizes, and also exposes its children list as public. On top of this however,
 * it allows users to layout the children by means of mouse and/or touch input.
 * <p>
 *
 * <h1>Dragging Nodes</h1>
 * <p>
 * By default, all of TactilePane's children are draggable, which means that a
 * user can drag them using mouse or touch input. This can be turned off by
 * setting the attached property {@link draggableProperty draggable} to
 * {@code false}.
 * <p>
 * To implement dragging of Nodes, Mouse/Touch events are handled (and consumed)
 * at the draggable node. In case of multi-touch gestures, only events from the
 * first touch point that interacted with the node will be processed. Calling
 * {@link getDragContext getDragContext} will provide information relevant to
 * the dragging operation on a node, such as the id of the touch point that is
 * being used for dragging. In {@link DragContext DragContext}, it's possible to
 * bind a drag operation to a new touch point, so that another touch point can
 * take the drag operation over.
 * <p>
 * The moment at which Mouse/Touch Events are handled to implement dragging can
 * be altered by setting the
 * {@link dragProcessingModeProperty dragProccesingMode}. This can be set so
 * that handling (and consuming) Mouse/Touch events happens during the filter or
 * the handler stage.
 */
@DefaultProperty("children")
public class TactilePane extends Region {
    // Attached Properties for Nodes
    static final String DRAG_CONTEXT = "tactile-pane-drag-context";
    
    /**
     * Whether Mouse/Touch events at this TactilePane's children should be processed and consumed
     * at the filtering stage or the handling stage.
     */
    private final EventProcessingMode dragProcessingMode;
    
    /**
     * Creates a TactilePane control 
     */
    public TactilePane(EventProcessingMode dragProcMode) {
        this.dragProcessingMode = dragProcMode;
    }

    public static DragContext getDragContext(Node node) {
        return (DragContext) node.getProperties().get(DRAG_CONTEXT);
    }
    
    public void addDragEventHandlers(final Node node) {
        if (getDragContext(node) != null) {
            return;
        }
        
        node.getProperties().put(DRAG_CONTEXT, new DragContext(node));
    }
    
    public void removeDragEventHandlers(Node node) {
        DragContext dragContext = getDragContext(node);
        if (dragContext != null) {
            dragContext.removeDragEventHandlers();
            node.getProperties().remove(DRAG_CONTEXT);
        }
    }

    public static void setGoToForegroundOnContact(Node node, boolean goToForegroundOnContact) {
        DragContext dragContext = getDragContext(node);
        if (dragContext != null) {
            dragContext.goToForegroundOnContact = goToForegroundOnContact;
        }
    }
    
   /**
     * @return modifiable list of children.
     */
    @Override public ObservableList<Node> getChildren() {
        return super.getChildren();
    }
    
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
    
    /**
     * Helper class used for dragging TactilePane's children.
     */
    public class DragContext {
        public static final int NULL_ID = -1;
        public static final int MOUSE_ID = -2;
        
        final Node node;         // Node that is being dragged
        /* Whether this node will go to the foreground when the user starts a drag gesture with it. */
        boolean goToForegroundOnContact; //
        double localX, localY;  // The x,y position of the Event in the Node
        int touchId;            // The id of the finger/cursor that is currently dragging the Node
        
        private final EventHandler<TouchEvent> touchHandler;
        private final EventHandler<MouseEvent> mouseHandler;
        
        private DragContext(Node draggable) {
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
                        Point2D local = TactilePane.this.sceneToLocal(event.getTouchPoint().getSceneX(), event.getTouchPoint().getSceneY());
                        this.handleTouchMoved(local.getX(), local.getY());
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
                        Point2D local = TactilePane.this.sceneToLocal(event.getSceneX(), event.getSceneY());
                        this.handleTouchMoved(local.getX(), local.getY());
                        event.consume();
                    }
                } else if (type == MouseEvent.MOUSE_RELEASED) {
                    if (this.touchId == DragContext.MOUSE_ID) {
                        this.touchId = DragContext.NULL_ID;
                        event.consume();
                    }
                }
            };
            
            if (dragProcessingMode == EventProcessingMode.FILTER) {
                draggable.addEventFilter(TouchEvent.ANY, touchHandler);
                draggable.addEventFilter(MouseEvent.ANY, mouseHandler);
            } else {
                draggable.addEventHandler(TouchEvent.ANY, touchHandler);
                draggable.addEventHandler(MouseEvent.ANY, mouseHandler);
            }
        }
        
        private void handleTouchPressed(double localX, double localY) {
            this.localX = localX;
            this.localY = localY;

            if (this.goToForegroundOnContact) {
                node.toFront();
            }
        }

        private void handleTouchMoved(double sceneX, double sceneY) {
            double x = sceneX - this.localX - node.getTranslateX();
            double y = sceneY - this.localY - node.getTranslateY();
            node.setLayoutX(x); 
            node.setLayoutY(y);
        }
        
        private void removeDragEventHandlers() {
            if (dragProcessingMode == EventProcessingMode.FILTER) {
                node.removeEventFilter(TouchEvent.ANY, touchHandler);
                node.removeEventFilter(MouseEvent.ANY, mouseHandler);
            } else {
                node.removeEventHandler(TouchEvent.ANY, touchHandler);
                node.removeEventHandler(MouseEvent.ANY, mouseHandler);
            }
        }
        
        /**
         * The Node that is being dragged
         */
        public Node getDraggable() {
            return this.node;
        }
        
        /**
         * The x location of the touchpoint/cursor that is currently dragging the Node
         */
        public double getLocalX() {
            return localX;
        }
        
        /**
         * The y location of the touchpoint/cursor that is currently dragging the Node
         */
        public double getLocalY() {
            return localY;
        }
        
        /**
         * The id of the TouchPoint that is responsible for dragging the Node.
         * Returns NULL_ID if the Node is not being dragged, or MOUSE_ID if the
         * Node is dragged by a mouse cursor.
         */
        public int getTouchId() {
            return touchId;
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
            return String.format("DragContext [draggable = %s, ,touchId = %d, localX = %f, localY = %f]", node.toString(), touchId, localX, localY);
        }
    }
}
