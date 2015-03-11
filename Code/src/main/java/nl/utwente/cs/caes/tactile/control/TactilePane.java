package nl.utwente.cs.caes.tactile.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import nl.utwente.cs.caes.tactile.event.TactilePaneEvent;
import nl.utwente.cs.caes.tactile.skin.TactilePaneSkin;

/**
 * <p>
 * A Control that allows a user to rearrange the position of its children. Acts
 * like a {@code Pane} in that it only resizes its children to its preferred
 * sizes, and also exposes its children list as public. On top of this however,
 * it allows users to layout the children by means of mouse and/or touch input.
 * It also has some basic "physics"-like features, such as collision detection,
 * and an event structure to go along with that.
 * <p>
 *
 * <h1>Dragging Nodes</h1>
 * <p>
 * By default, all of TactilePane's children are draggable, which means that a
 * user can drag them using mouse or touch input. This can be turned off by
 * setting the attached property {@link draggableProperty draggable} to
 * {@code false}. To prevent the user from dragging a node beyond the bounds of
 * the TactilePane, the {@link bordersCollideProperty borderCollide} property
 * can be set to {@code true}.
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
 * <p>
 * <h1>Active nodes and Events</h1>
 * <p>
 * <h1>Physics</h1>
 * <p>
 */
@DefaultProperty("children")
public class TactilePane extends Control {
    // Attached Properties for Nodes
    static final String IN_USE = "tactile-pane-in-use";
    static final String ANCHOR = "tactile-pane-anchor";
    static final String VECTOR = "tactile-pane-vector";
    static final String GO_TO_FOREGROUND_ON_CONTACT = "tactile-pane-go-to-foreground-on-contact";
    static final String DRAGGABLE = "tactile-pane-draggable";
    static final String SLIDE_ON_RELEASE = "tactile-pane-slide-on-release";
    static final String NODES_COLLIDING = "tactile-pane-nodes-colliding";
    static final String NODES_PROXIMITY = "tactile-pane-nodes-proximity";
    static final String NODES_BOND = "tactile-pane-nodes-bond";
    static final String TRACKER = "tactile-pane-tracker";
    static final String ON_PROXIMITY_ENTERED = "tactile-pane-on-proximity-entered";
    static final String ON_PROXIMITY_LEFT = "tactile-pane-on-proximity-left";
    static final String ON_IN_PROXIMITY = "tactile-pane-on-in-proximity";
    static final String ON_AREA_ENTERED = "tactile-pane-on-area-entered";
    static final String ON_AREA_LEFT = "tactile-pane-on-area-left";
    static final String ON_IN_AREA = "tactile-pane-on-in-area";
    static final String DRAG_CONTEXT = "tactile-pane-drag-context";
    
    // Attached Properties for Nodes that are only used privately
    static final String TOUCH_EVENT_HANDLER = "tactile-pane-touch-event-handler";
    static final String MOUSE_EVENT_HANDLER = "tactile-pane-mouse-event-handler";
    
    // ATTACHED PROPERTIES
    private static void setDragContext(Node node, DragContext dragContext) {
        setConstraint(node, DRAG_CONTEXT, dragContext);
    }
    
    public static DragContext getDragContext(Node node) {
        return (DragContext) getConstraint(node, DRAG_CONTEXT);
    }
    
    static void setInUse(Node node, boolean inUse) {
        inUsePropertyImpl(node).set(inUse);
    }
    
    public static boolean isInUse(Node node) {
        return inUsePropertyImpl(node).get();
    }
    
    static BooleanProperty inUsePropertyImpl(Node node) {
        BooleanProperty property = (BooleanProperty) getConstraint(node, IN_USE);
        if (property == null) {
            property = new SimpleBooleanProperty(false);
            setConstraint(node, IN_USE, property);
        }
        return property;
    }
    
    /**
     * Whether this {@code Node} is being dragged by the user. If the {@code Node}
     * is not a child of a {@code TactilePane}, it will always return {@code false}.
     */
    public static ReadOnlyBooleanProperty inUseProperty(Node node) {
        return inUsePropertyImpl(node);
    }
    
    public static void setAnchor(Node node, Anchor anchor) {
        anchorProperty(node).set(anchor);
    }
    
    public static Anchor getAnchor(Node node) {
        return anchorProperty(node).get();
    }
    
    // TODO: Rewrite JavaDoc
    public static ObjectProperty<Anchor> anchorProperty(Node node) {
        ObjectProperty<Anchor> property = (ObjectProperty<Anchor>) getConstraint(node, ANCHOR);
        if (property == null) {
            property = new SimpleObjectProperty<>(null);
            setConstraint(node, ANCHOR, property);
        }
        return property;
    }
    
    public static void setVector(Node node, Point2D vector) {
        vectorProperty(node).set(vector);
    }
    
    public static Point2D getVector(Node node) {
        return vectorProperty(node).get();
    }
    
    /**
     * The 2D velocity vector for this {@code node}. Primarily intended for physics.
     */
    public static ObjectProperty<Point2D> vectorProperty(Node node) {
        ObjectProperty<Point2D> property = (ObjectProperty<Point2D>) getConstraint(node, VECTOR);
        if (property == null) {
            property = new SimpleObjectProperty<>(Point2D.ZERO);
            setConstraint(node, VECTOR, property);
        }
        return property;
    }
    
    public static void setGoToForegroundOnContact(Node node, boolean goToForegroundOnContact) {
        goToForegroundOnContactProperty(node).set(goToForegroundOnContact);
    }
    
    public static boolean isGoToForegroundOnContact(Node node) {
        return goToForegroundOnContactProperty(node).get();
    }
    
    /**
     * Whether this {@code node} will go to the foreground when the user starts
     * a drag gesture with it.
     * 
     * @defaultValue true
     */
    public static BooleanProperty goToForegroundOnContactProperty(Node node) {
        BooleanProperty property = (BooleanProperty) getConstraint(node, GO_TO_FOREGROUND_ON_CONTACT);
        if (property == null) {
            property = new SimpleBooleanProperty(true);
            setConstraint(node, GO_TO_FOREGROUND_ON_CONTACT, property);
        }
        return property;
    }
    
    public static void setDraggable(Node node, boolean draggable) {
        draggableProperty(node).set(draggable);
    }
    
    public static boolean isDraggable(Node node) {
        return draggableProperty(node).get();
    }
    
    /**
     * Whether the given node can be dragged by the user. Only nodes that are a direct child of
     * a {@code TactilePane} can be dragged.
     * 
     * @defaultValue true
     */
    public static BooleanProperty draggableProperty(Node node) {
        BooleanProperty property = (BooleanProperty) getConstraint(node, DRAGGABLE);
        if (property == null) {
            property = new SimpleBooleanProperty(true) {
                @Override
                public void set(boolean draggable) {
                    if (!draggable) {
                        // A node that is not draggable cannot be in use
                        setInUse(node, false);
                    }
                    super.set(draggable);
                }
            };
            setConstraint(node, DRAGGABLE, property);
        }
        return property;
    }
    
    public static void setSlideOnRelease(Node node, boolean slideOnRelease) {
        slideOnReleaseProperty(node).set(slideOnRelease);
    }
    
    public static boolean isSlideOnRelease(Node node) {
        return slideOnReleaseProperty(node).get();
    }
    
    /**
     * Whether the given {@code Node} will get a vector in the direction it was
     * moving when the user stops dragging that {@code Node}
     *
     * @defaultValue false
     */
    public static BooleanProperty slideOnReleaseProperty(Node node) {
        BooleanProperty property = (BooleanProperty) getConstraint(node, SLIDE_ON_RELEASE);
        if (property == null) {
            property = new SimpleBooleanProperty(false);
            setConstraint(node, SLIDE_ON_RELEASE, property);
        }
        return property;
    }
    
    /**
     * Returns the set of {@code Nodes} that are registered to the same
     * {@code TactilePane} as the given {@code node}, and are currently
     * colliding with that {@code node}
     */
    public static ObservableSet<Node> getNodesColliding(Node node) {
        ObservableSet<Node> result = (ObservableSet<Node>) getConstraint(node, NODES_COLLIDING);
        if (result == null) {
            result = FXCollections.observableSet(new HashSet<Node>());
            setConstraint(node, NODES_COLLIDING, result);
        }
        return result;
    }
    
    /**
     * Returns the set of {@code Nodes} that are registered to the same
     * {@code TactilePane} as the given {@code node}, and are currently in the
     * proximity of that {@code node}
     */
    public static ObservableSet<Node> getNodesInProximity(Node node) {
        ObservableSet<Node> result = (ObservableSet<Node>) getConstraint(node, NODES_PROXIMITY);
        if (result == null) {
            result = FXCollections.observableSet(new HashSet<Node>());
            setConstraint(node, NODES_PROXIMITY, result);
        }
        return result;
    }
    
    /**
     * Returns the set of {@code Bonds} associated with the given {@code Node}. If the set
     * already contains a {@code Bond} with the same {@code bondNode}, the old {@code Bond}
     * is replaced.
     */
    public static ObservableSet<Bond> getBonds(Node node) {
	ObservableSet<Bond> result = (ObservableSet<Bond>) getConstraint(node, NODES_BOND);
        if (result == null) {
            result = FXCollections.observableSet(new HashSet<Bond>() {
                @Override
                public boolean add(Bond bond) {
                    Optional<Bond> opt = stream().filter(b -> b.getBondNode() == bond.getBondNode()).findAny();
                    if (opt.isPresent()) {
                        remove(opt.get());
                    }
                    return super.add(bond);
                }
            });
            setConstraint(node, NODES_BOND, result);
        }
        return result;
    }
    
    
    public static void setOnInProximity(Node node, EventHandler<? super TactilePaneEvent> handler) {
        onInProximityProperty(node).set(handler);
    }
    
    public static EventHandler<? super TactilePaneEvent> getOnInProximity(Node node) {
        return onInProximityProperty(node).get();
    }
    
    /**
     * Defines a function to be called continuously when another {@code Node} is
     * in the proximity of this {@code node}.
     */
    public static ObjectProperty<EventHandler<? super TactilePaneEvent>> onInProximityProperty(Node node) {
        ObjectProperty<EventHandler<? super TactilePaneEvent>> property = (ObjectProperty<EventHandler<? super TactilePaneEvent>>) getConstraint(node, ON_IN_PROXIMITY);
        if (property == null) {
            property = new SimpleObjectProperty<EventHandler<? super TactilePaneEvent>>(null) {
                @Override
                public void set(EventHandler<? super TactilePaneEvent> handler) {
                    EventHandler<? super TactilePaneEvent> oldHandler = get();
                    if (oldHandler != null) {
                        node.removeEventHandler(TactilePaneEvent.IN_PROXIMITY, oldHandler);
                    }
                    if (handler != null) {
                        node.addEventHandler(TactilePaneEvent.IN_PROXIMITY, handler);
                    }
                    super.set(handler);
                }
            };
            setConstraint(node, ON_IN_PROXIMITY, property);
        }
        return property;
    }
    
    public static void setOnProximityEntered(Node node, EventHandler<? super TactilePaneEvent> handler) {
        onProximityEnteredProperty(node).set(handler);
    }
    
    public static EventHandler<? super TactilePaneEvent> getOnProximityEntered(Node node) {
        return onProximityEnteredProperty(node).get();
    }
    
    /**
     * Defines a function to be called when another {@code Node} enters the
     * proximity of this {@code node}.
     */
    public static ObjectProperty<EventHandler<? super TactilePaneEvent>> onProximityEnteredProperty(Node node) {
        ObjectProperty<EventHandler<? super TactilePaneEvent>> property = (ObjectProperty<EventHandler<? super TactilePaneEvent>>) getConstraint(node, ON_PROXIMITY_ENTERED);
        if (property == null) {
            property = new SimpleObjectProperty<EventHandler<? super TactilePaneEvent>>(null) {
                @Override
                public void set(EventHandler<? super TactilePaneEvent> handler) {
                    EventHandler<? super TactilePaneEvent> oldHandler = get();
                    if (oldHandler != null) {
                        node.removeEventHandler(TactilePaneEvent.PROXIMITY_ENTERED, oldHandler);
                    }
                    if (handler != null) {
                        node.addEventHandler(TactilePaneEvent.PROXIMITY_ENTERED, handler);
                    }
                    super.set(handler);
                }
            };
            setConstraint(node, ON_PROXIMITY_ENTERED, property);
        }
        return property;
    }
    
    public static void setOnProximityLeft(Node node, EventHandler<? super TactilePaneEvent> handler) {
        onProximityLeftProperty(node).set(handler);
    }
    
    public static EventHandler<? super TactilePaneEvent> getOnProximityLeft(Node node) {
        return onProximityLeftProperty(node).get();
    }
    
    /**
     * Defines a function to be called when another {@code Node} leaves the
     * proximity of this {@code node}.
     */
    public static ObjectProperty<EventHandler<? super TactilePaneEvent>> onProximityLeftProperty(Node node) {
        ObjectProperty<EventHandler<? super TactilePaneEvent>> property = (ObjectProperty<EventHandler<? super TactilePaneEvent>>) getConstraint(node, ON_PROXIMITY_LEFT);
        if (property == null) {
            property = new SimpleObjectProperty<EventHandler<? super TactilePaneEvent>>(null) {
                @Override
                public void set(EventHandler<? super TactilePaneEvent> handler) {
                    EventHandler<? super TactilePaneEvent> oldHandler = get();
                    if (oldHandler != null) {
                        node.removeEventHandler(TactilePaneEvent.PROXIMITY_LEFT, oldHandler);
                    }
                    if (handler != null) {
                        node.addEventHandler(TactilePaneEvent.PROXIMITY_LEFT, handler);
                    }
                    super.set(handler);
                }
            };
            setConstraint(node, ON_PROXIMITY_LEFT, property);
        }
        return property;
    }
    
    public static void setOnInArea(Node node, EventHandler<? super TactilePaneEvent> handler) {
        onInAreaProperty(node).set(handler);
    }
    
    public static EventHandler<? super TactilePaneEvent> getOnInArea(Node node) {
        return onInAreaProperty(node).get();
    }
    
    /**
     * Defines a function to be called continuously when another {@code Node} is
     * in the bounds of this {@code node}.
     */
    public static ObjectProperty<EventHandler<? super TactilePaneEvent>> onInAreaProperty(Node node) {
        ObjectProperty<EventHandler<? super TactilePaneEvent>> property = (ObjectProperty<EventHandler<? super TactilePaneEvent>>) getConstraint(node, ON_IN_AREA);
        if (property == null) {
            property = new SimpleObjectProperty<EventHandler<? super TactilePaneEvent>>(null) {
                @Override
                public void set(EventHandler<? super TactilePaneEvent> handler) {
                    EventHandler<? super TactilePaneEvent> oldHandler = get();
                    if (oldHandler != null) {
                        node.removeEventHandler(TactilePaneEvent.IN_AREA, oldHandler);
                    }
                    if (handler != null) {
                        node.addEventHandler(TactilePaneEvent.IN_AREA, handler);
                    }
                    super.set(handler);
                }
            };
            setConstraint(node, ON_IN_AREA, property);
        }
        return property;
    }
    
    public static void setOnAreaEntered(Node node, EventHandler<? super TactilePaneEvent> handler) {
        onAreaEnteredProperty(node).set(handler);
    }
    
    public static EventHandler<? super TactilePaneEvent> getOnAreaEntered(Node node) {
        return onAreaEnteredProperty(node).get();
    }
    
    /**
     * Defines a function to be called when another {@code Node} enters the
     * bounds of this {@code node}.
     */
    public static ObjectProperty<EventHandler<? super TactilePaneEvent>> onAreaEnteredProperty(Node node) {
        ObjectProperty<EventHandler<? super TactilePaneEvent>> property = (ObjectProperty<EventHandler<? super TactilePaneEvent>>) getConstraint(node, ON_AREA_ENTERED);
        if (property == null) {
            property = new SimpleObjectProperty<EventHandler<? super TactilePaneEvent>>(null) {
                @Override
                public void set(EventHandler<? super TactilePaneEvent> handler) {
                    EventHandler<? super TactilePaneEvent> oldHandler = get();
                    if (oldHandler != null) {
                        node.removeEventHandler(TactilePaneEvent.AREA_ENTERED, oldHandler);
                    }
                    if (handler != null) {
                        node.addEventHandler(TactilePaneEvent.AREA_ENTERED, handler);
                    }
                    super.set(handler);
                }
            };
            setConstraint(node, ON_AREA_ENTERED, property);
        }
        return property;
    }
    
    public static void setOnAreaLeft(Node node, EventHandler<? super TactilePaneEvent> handler) {
        onAreaLeftProperty(node).set(handler);
    }
    
    public static EventHandler<? super TactilePaneEvent> getOnAreaLeft(Node node) {
        return onAreaLeftProperty(node).get();
    }
    
    /**
     * Defines a function to be called when another {@code Node} leaves the
     * bounds of this {@code node}.
     */
    public static ObjectProperty<EventHandler<? super TactilePaneEvent>> onAreaLeftProperty(Node node) {
        ObjectProperty<EventHandler<? super TactilePaneEvent>> property = (ObjectProperty<EventHandler<? super TactilePaneEvent>>) getConstraint(node, ON_AREA_LEFT);
        if (property == null) {
            property = new SimpleObjectProperty<EventHandler<? super TactilePaneEvent>>(null) {
                @Override
                public void set(EventHandler<? super TactilePaneEvent> handler) {
                    EventHandler<? super TactilePaneEvent> oldHandler = get();
                    if (oldHandler != null) {
                        node.removeEventHandler(TactilePaneEvent.AREA_LEFT, oldHandler);
                    }
                    if (handler != null) {
                        node.addEventHandler(TactilePaneEvent.AREA_LEFT, handler);
                    }
                    super.set(handler);
                }
            };
            setConstraint(node, ON_AREA_LEFT, property);
        }
        return property;
    }
    
    /**
     * Calls {@code register} on the given TactilePane with {@code node} as
     * argument. If {@code tactilePane} is {@code null}, {@code node} will be
     * deregistered at its previous {@code TactilePane}, if one exists.
     */
    public static void setTracker(Node node, TactilePane tactilePane) {
        if (tactilePane == null) {
            TactilePane oldPane = getTracker(node);
            if (oldPane != null) {
                oldPane.getActiveNodes().remove(node);
            }
        } else {
            tactilePane.getActiveNodes().add(node);
        }
    }
    
    /**
     * The {@code TactilePane} which is currently tracking {@code node}.
     */
    public static TactilePane getTracker(Node node) {
        return (TactilePane) getConstraint(node, TRACKER);
    }
    
    // Used to attach a Property to a Node
    static void setConstraint(Node node, Object key, Object value) {
        if (value == null) {
            node.getProperties().remove(key);
        } else {
            node.getProperties().put(key, value);
        }
        if (node.getParent() != null) {
            node.getParent().requestLayout();
        }
    }

    static Object getConstraint(Node node, Object key) {
        if (node.hasProperties()) {
            Object value = node.getProperties().get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
    
    
    // STATIC METHODS
    
    /**
     * Gives the {@code move} Node a velocity vector with a direction so that it will move
     * away from the {@code from} Node. The speed with which the Node moves away
     * depends on {@code force}, which is the magniute of the vector.
     * @param move  The Node that should move away
     * @param from  The Node it should move away from
     * @param force The magnitude of the vector that the Node gets
     */
    public static void moveAwayFrom(Node move, Node from, double force) {
        if (move.getParent() == null) return;
        
        Node moveDraggable = move;
        while(!(moveDraggable.getParent() instanceof TactilePane)) {
            moveDraggable = moveDraggable.getParent();
            if (move.getParent() == null) return;
        }
        
        while (getAnchor(moveDraggable) != null) {
            moveDraggable = getAnchor(moveDraggable).getAnchorNode();
            
            while(!(moveDraggable.getParent() instanceof TactilePane)) {
                moveDraggable = moveDraggable.getParent();
                if (move.getParent() == null) return;
            }
        }
        
        Bounds moveBounds = move.localToScene(move.getBoundsInLocal());
        Bounds fromBounds = from.localToScene(from.getBoundsInLocal());

        double moveX = moveBounds.getMinX() + moveBounds.getWidth() / 2;
        double moveY = moveBounds.getMinY() + moveBounds.getHeight() / 2;
        double fromX = fromBounds.getMinX() + fromBounds.getWidth() / 2;
        double fromY = fromBounds.getMinY() + fromBounds.getHeight() / 2;
        
        Point2D vector = new Point2D(moveX - fromX, moveY - fromY).normalize().multiply(force);
        TactilePane.setVector(moveDraggable, TactilePane.getVector(move).add(vector));
    }
    
    /**
     * Gives the {@code move} Node a velocity vector with a direction so that it will move
     * away from the {@code from} Node. Moves with a default level of force.
     * @param move  The Node that should move away
     * @param from  The Node it should move away from
     */
    public static void moveAwayFrom(Node move, Node from) {
    	moveAwayFrom(move, from, PhysicsTimer.DEFAULT_FORCE);
    }
    
    // INSTANCE VARIABLES
    private final PhysicsTimer physics;
    //TODO: uiteindelijke package-private, alleen voor debug
    public final QuadTree quadTree;
    private final ObservableSet<Node> activeNodes;
    
    // CONSTRUCTORS
    
    /**
     * Creates a TactilePane control 
     */
    public TactilePane() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        // Since this Control is more or less a Pane, focusTraversable should be false by default
        ((StyleableProperty<Boolean>)focusTraversableProperty()).applyStyle(null, false);
        
        // Add EventHandlers for dragging to children when they are added
        super.getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            while(c.next()) {
                for (Node node: c.getRemoved()) {
                    // Delay removal of drag event handlers, just in case all that
                    // happened is a node.toFront() call.

                    // TODO: Rewrite code so this ugly workaround isn't necessary
                    TimerTask removeDragEventHandlers = new TimerTask() {
                        @Override
                        public void run() {
                            if (node.getParent() != TactilePane.this) {
                                removeDragEventHandlers(node);
                            }
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(removeDragEventHandlers, 500);
                }
                for (Node node: c.getAddedSubList()) {
                    addDragEventHandlers(node);
                }
            }
        });
        
        // Initialise quadTree
        quadTree = new QuadTree(this.localToScene(this.getBoundsInLocal()));
        this.widthProperty().addListener((observableValue, oldWidth, newWidth) -> {
            quadTree.setBounds(this.localToScene(this.getBoundsInLocal()));
        });
        this.heightProperty().addListener((observableValue, oldHeight, newHeight) -> {
            quadTree.setBounds(this.localToScene(this.getBoundsInLocal()));
        });
        
        // Initialise activeNodes
        activeNodes = FXCollections.observableSet(Collections.newSetFromMap(new ConcurrentHashMap<>()));
        activeNodes.addListener((SetChangeListener.Change<? extends Node> change) -> {
            if (change.wasAdded()) {
                Node node = change.getElementAdded();
                TactilePane oldPane = getTracker(node);
                if (oldPane != null) {
                    oldPane.getActiveNodes().remove(node);
                }
                quadTree.insert(node);
                setConstraint(node, TRACKER, TactilePane.this);
            }
            else {
                Node node = change.getElementRemoved();
                quadTree.insert(node);
                
                for (Node colliding : TactilePane.getNodesColliding(node)) {
                    node.fireEvent(new TactilePaneEvent(TactilePaneEvent.AREA_LEFT, node, colliding));
                    colliding.fireEvent(new TactilePaneEvent(TactilePaneEvent.AREA_LEFT, colliding, node));
                }
                TactilePane.getNodesColliding(node).clear();
                
                for (Node colliding : TactilePane.getNodesInProximity(node)) {
                    node.fireEvent(new TactilePaneEvent(TactilePaneEvent.PROXIMITY_LEFT, node, colliding));
                    colliding.fireEvent(new TactilePaneEvent(TactilePaneEvent.PROXIMITY_LEFT, colliding, node));
                }
                TactilePane.getNodesInProximity(node).clear();
                
                setConstraint(node, TRACKER, null);
            }
        });
        
        // Initialise Physics
        physics = new PhysicsTimer(this);
        physics.start();
    }
    
    /**
     * Creates a TactilePane control
     * @param children The initial set of children for this TactilePane
     */
    public TactilePane(Node... children) {
        this();
        super.getChildren().addAll(children);
    }
    
    // MAKING CHILDREN DRAGGABLE
    
    private void addDragEventHandlers(final Node node) {
        if (getDragContext(node) != null) {
            // The node already has drag event handlers
            return;
        }
        
        final DragContext dragContext = new DragContext(node);
        
        EventHandler<TouchEvent> touchHandler = event -> {
            if (!isDraggable(node)) return;
            
            EventType type = event.getEventType();
            
            if (type == TouchEvent.TOUCH_PRESSED) {
                if (dragContext.touchId == DragContext.NULL_ID) {
                    dragContext.touchId = event.getTouchPoint().getId();
                    handleTouchPressed(node, event.getTouchPoint().getX(), event.getTouchPoint().getY());
                    event.consume();
                }
            } else if (type == TouchEvent.TOUCH_MOVED) {
                if (dragContext.touchId == event.getTouchPoint().getId()) {
                    handleTouchMoved(node, event.getTouchPoint().getSceneX(), event.getTouchPoint().getSceneY());
                    event.consume();
                }
            } else if (type == TouchEvent.TOUCH_RELEASED) {
                if (dragContext.touchId == event.getTouchPoint().getId()) {
                    handleTouchReleased(node);
                    dragContext.touchId = DragContext.NULL_ID;
                    event.consume();
                }
            }
        };
        
        EventHandler<MouseEvent> mouseHandler = event -> {
            if (!isDraggable(node)) return;
            
            EventType type = event.getEventType();
            
            if (type == MouseEvent.MOUSE_PRESSED) {
                if (dragContext.touchId == DragContext.NULL_ID) {
                    dragContext.touchId = DragContext.MOUSE_ID;
                    handleTouchPressed(node, event.getX(), event.getY());
                    event.consume();
                }
            } else if (type == MouseEvent.MOUSE_DRAGGED) {
                
                if (dragContext.touchId == DragContext.MOUSE_ID) {
                    handleTouchMoved(node, event.getSceneX(), event.getSceneY());
                    event.consume();
                }
            } else if (type == MouseEvent.MOUSE_RELEASED) {
                if (dragContext.touchId == DragContext.MOUSE_ID) {
                    handleTouchReleased(node);
                    dragContext.touchId = DragContext.NULL_ID;
                    event.consume();
                }
            }
        };
        
        setDragContext(node, dragContext);
        setConstraint(node, TOUCH_EVENT_HANDLER, touchHandler);
        setConstraint(node, MOUSE_EVENT_HANDLER, mouseHandler);
        
        if (getDragProcessingMode() == EventProcessingMode.FILTER) {
            node.addEventFilter(TouchEvent.ANY, touchHandler);
            node.addEventFilter(MouseEvent.ANY, mouseHandler);
        } else {
            node.addEventHandler(TouchEvent.ANY, touchHandler);
            node.addEventHandler(MouseEvent.ANY, mouseHandler);
        }
    }
    
    private void removeDragEventHandlers(Node node) {
        EventHandler<TouchEvent> touchHandler = (EventHandler<TouchEvent>) getConstraint(node, TOUCH_EVENT_HANDLER);
        EventHandler<MouseEvent> mouseHandler = (EventHandler<MouseEvent>) getConstraint(node, MOUSE_EVENT_HANDLER);
        
        if (getDragProcessingMode() == EventProcessingMode.FILTER) {
            node.removeEventFilter(TouchEvent.ANY, touchHandler);
            node.removeEventFilter(MouseEvent.ANY, mouseHandler);
        } else {
            node.removeEventHandler(TouchEvent.ANY, touchHandler);
            node.removeEventHandler(MouseEvent.ANY, mouseHandler);
        }
        
        setDragContext(node, null);
        setConstraint(node, TOUCH_EVENT_HANDLER, null);
        setConstraint(node, MOUSE_EVENT_HANDLER, null);
    }
    
    private void handleTouchPressed(Node node, double localX, double localY) {
        DragContext dragContext = getDragContext(node);
        setAnchor(node, null);
        setInUse(node, true);
        setVector(node, Point2D.ZERO);

        dragContext.localX = localX;
        dragContext.localY = localY;

        if (isGoToForegroundOnContact(node)) {
            node.toFront();
        }
    }

    private void handleTouchMoved(Node node, double sceneX, double sceneY) {
        DragContext dragContext = getDragContext(node);
        if (getAnchor(node) == null) {
            double x = sceneX - dragContext.localX - node.getTranslateX();
            double y = sceneY - dragContext.localY - node.getTranslateY();

            if (isBordersCollide()) {
                Bounds paneBounds = this.getBoundsInLocal();
                Bounds nodeBounds = node.getBoundsInParent();
                
                double deltaX = node.getLayoutX() - nodeBounds.getMinX();
                double deltaY = node.getLayoutY() - nodeBounds.getMinY();

                if (x - deltaX < paneBounds.getMinX()) {
                    x = paneBounds.getMinX() + deltaX;
                } else if (x - deltaX + nodeBounds.getWidth() > paneBounds.getMaxX()) {
                    x = paneBounds.getMaxX() - nodeBounds.getWidth() + deltaX;
                }
                if (y - deltaY < paneBounds.getMinY()) {
                    y = paneBounds.getMinY() + deltaY;
                } else if (y - deltaY + nodeBounds.getHeight() > paneBounds.getMaxY()) {
                    y = paneBounds.getMaxY() - nodeBounds.getHeight() + deltaY;
                }
            }
            node.setLayoutX(x); 
            node.setLayoutY(y);
        }
    }

    private void handleTouchReleased(Node node) {
        setInUse(node, false);
    }
    
    // INSTANCE PROPERTIES
    
   /**
     *
     * @return modifiable list of children.
     */
    @Override public ObservableList<Node> getChildren() {
        return super.getChildren();
    }
    
    /**
     * 
     * @return modifiable list of {@code Nodes} that are tracked by this {@code TactilePane}
     */
    public ObservableSet<Node> getActiveNodes() {
        return activeNodes;
    }
    
    /**
     * Whether Mouse/Touch events at this TactilePane's children should be processed and consumed
     * at the filtering stage or the handling stage.
     * 
     * @defaultValue EventProcessingMode.HANDLER
     */
    private ObjectProperty<EventProcessingMode> dragProcessingMode;
    
    public void setDragProcessingMode(EventProcessingMode mode) {
        dragProcessingModeProperty().set(mode);
    }
    
    public EventProcessingMode getDragProcessingMode() {
        return dragProcessingModeProperty().get();
    }
    
    public ObjectProperty<EventProcessingMode> dragProcessingModeProperty() {
        if (dragProcessingMode == null) {
            dragProcessingMode = new SimpleObjectProperty<EventProcessingMode>(EventProcessingMode.HANDLER) {
                
                @Override
                public void set(EventProcessingMode value) {
                    for (Node node : TactilePane.this.getChildren()) {
                        removeDragEventHandlers(node);
                    }
                    super.set(value);
                    for (Node node : TactilePane.this.getChildren()) {
                        addDragEventHandlers(node);
                    }
                }
            };
        }
        return dragProcessingMode;
    }
    
    /**
     * Whether children will collide with the borders of this
     * {@code TactilePane}. If set to true the {@code TactilePane} will prevent
     * children that are moving because of user input or physics to
     * move outside of the {@code TactilePane's} boundaries.
     *
     * @defaultValue false
     */
    private BooleanProperty bordersCollide;

    public final void setBordersCollide(boolean value) {
        bordersCollideProperty().set(value);
    }

    public final boolean isBordersCollide() {
        return bordersCollideProperty().get();
    }

    public final BooleanProperty bordersCollideProperty() {
        if (bordersCollide == null) {
            bordersCollide = new SimpleBooleanProperty(false);
        }
        return bordersCollide;
    }
    
    public final void setProximityThreshold(double threshold) {
        proximityThresholdProperty().set(threshold);
    }

    public final double getProximityThreshold() {
        return proximityThresholdProperty().get();
    }

    /**
     * Specifies how close two {@code Nodes} have to be to each other to be
     * considered in each others proximity. When set to 0, TactilePane won't fire
     * {@code PROXIMITY_ENTERED} or {@code IN_PROXIMITY} events at all.
     * {@code PROXIMITY_LEFT} events will still be fired for any pair of
     * {@code Nodes} that entered each other's proximity before the threshold
     * was set to 0. When set to a negative value, an IllegalArgumentException
     * is thrown.
     *
     * @defaultValue 25.0
     */
    public final DoubleProperty proximityThresholdProperty() {
        return quadTree.proximityThresholdProperty();
    }
    
    // STYLESHEET HANDLING
    
    // The selector class
    private static String DEFAULT_STYLE_CLASS = "tactile-pane";
    
    private static final class StyleableProperties {
        // TODO make properties stylable using CSS

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
                final List<CssMetaData<? extends Styleable, ?>> styleables = 
                    new ArrayList<>(Control.getClassCssMetaData());

                STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
    
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<TactilePane> createDefaultSkin() {
        return new TactilePaneSkin(this);
    }
    
    // ENUMS
    
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
    
    // NESTED CLASSES

    /**
     * Help class used for dragging TactilePane's children.
     */
    public class DragContext {
        public static final int NULL_ID = -1;
        public static final int MOUSE_ID = -2;
        
        final Node draggable;         // Node that is being dragged
        double localX, localY;  // The x,y position of the Event in the Node
        int touchId;            // The id of the finger/cursor that is currently dragging the Node
        
        private DragContext(Node draggable) {
            this.draggable = draggable;
            touchId = -1;
        }
        
        /**
         * The Node that is being dragged
         */
        public Node getDraggable() {
            return draggable;
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
            while (target.getParent() != draggable) {
                target = target.getParent();
                if (target == null) {
                    throw new IllegalArgumentException("TouchEvent's target should be draggable, or have draggable as ancestor");
                }
            }
            if (event.getEventType() != TouchEvent.TOUCH_PRESSED) {
                throw new IllegalArgumentException("TouchEvent should be of type TOUCH_PRESSED");
            }
            
            touchId = event.getTouchPoint().getId();
            handleTouchPressed(draggable, event.getTouchPoint().getX(), event.getTouchPoint().getY());
        }
        
        @Override
        public String toString() {
            return String.format("DragContext [draggable = %s, ,touchId = %d, localX = %f, localY = %f]", draggable.toString(), touchId, localX, localY);
        }
    }
}
