package nl.utwente.ewi.caes.tactilefx;

import javafx.beans.DefaultProperty;
import javafx.collections.*;
import javafx.scene.Node;
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
        
        node.getProperties().put(DRAG_CONTEXT, new DragContext(node, dragProcessingMode));
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
            dragContext.setGoToForegroundOnContact(goToForegroundOnContact);
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
}
