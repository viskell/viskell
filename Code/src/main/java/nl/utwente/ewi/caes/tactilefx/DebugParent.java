package nl.utwente.ewi.caes.tactilefx;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class DebugParent extends StackPane {
    
    Pane overlay = new Pane();
    Map<Integer, TouchDisplay> circleByTouchId = new TreeMap<>();
    Map<Integer, Line> lineByTouchId = new TreeMap<>();

    Map<Node, ProximityDisplay> proximityDisplayByNode = new ConcurrentHashMap<>();
    Map<Node, BoundsDisplay> boundsDisplayByNode = new ConcurrentHashMap<>();

    List<TouchPoint> touchPoints = new ArrayList<>();
    int touchSetId = 0;
    boolean active = false;

    public DebugParent(Node node) {
        super(node);
        initialise();
    }

    private void initialise() {
        overlay.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));

        // Overlay shouldn't receive events
        overlay.setDisable(true);
        
        overlay.visibleProperty().bind(overlayVisibleProperty());

        // Makes sure the overlay is always drawn on top of the other child
        getChildren().add(overlay);
        getChildren().addListener((Observable value) -> {
            overlay.toFront();
        });
        
        // Maps mouse events to touch events
        addEventFilter(MouseEvent.ANY, event -> {
            if (getMapMouseToTouch() && !event.isSynthesized()) {
                if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                    TouchPoint tp = createTouchPoint(event);
                    TouchEvent tEvent = new TouchEvent(this, event.getTarget(), TouchEvent.TOUCH_PRESSED,
                            tp, touchPoints, touchSetId, event.isShiftDown(), event.isControlDown(),
                            event.isAltDown(), event.isMetaDown());
                    Event.fireEvent(event.getTarget(), tEvent);
                } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    TouchPoint tp = createTouchPoint(event);
                    TouchEvent tEvent = new TouchEvent(this, event.getTarget(), TouchEvent.TOUCH_MOVED,
                            tp, touchPoints, touchSetId, event.isShiftDown(), event.isControlDown(),
                            event.isAltDown(), event.isMetaDown());
                    Event.fireEvent(event.getTarget(), tEvent);
                } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                    TouchPoint tp = createTouchPoint(event);
                    TouchEvent tEvent = new TouchEvent(this, event.getTarget(), TouchEvent.TOUCH_RELEASED,
                            tp, touchPoints, touchSetId, event.isShiftDown(), event.isControlDown(),
                            event.isAltDown(), event.isMetaDown());
                    Event.fireEvent(event.getTarget(), tEvent);

                    touchSetId++;
                }
                // Send synthesized MouseEvent
                MouseEvent mouseEvent = new MouseEvent(event.getSource(), event.getTarget(), event.getEventType(),
                        event.getSceneX(), event.getSceneY(), event.getScreenX(), event.getScreenY(), event.getButton(),
                        event.getClickCount(), event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown(),
                        event.isPrimaryButtonDown(), event.isMiddleButtonDown(), event.isSecondaryButtonDown(), true,
                        event.isPopupTrigger(), event.isStillSincePress(), event.getPickResult());
                Event.fireEvent(event.getTarget(), mouseEvent);
                event.consume();
            }
        });

        addEventFilter(TouchEvent.TOUCH_PRESSED, event -> {
            int touchId = event.getTouchPoint().getId();
            Node target = (Node) event.getTarget();
            Bounds bounds = target.localToScene(target.getBoundsInLocal());

            double x = event.getTouchPoint().getSceneX();
            double y = event.getTouchPoint().getSceneY();

            TouchDisplay circle = new TouchDisplay(x, y, getTouchCircleRadius(), touchId);
            circleByTouchId.put(touchId, circle);
            overlay.getChildren().add(circle);

            Line line = new Line(x, y, bounds.getMinX(), bounds.getMinY());
            lineByTouchId.put(touchId, line);
            overlay.getChildren().add(line);

            circle.relocate(x, y);

            ScaleTransition st = new ScaleTransition(new Duration(200), circle);
            st.setFromX(0);
            st.setFromY(0);
            st.setToX(1);
            st.setToY(1);

            FadeTransition ft = new FadeTransition(new Duration(200), line);
            ft.setFromValue(0);
            ft.setToValue(1);

            st.play();
            ft.play();
        });

        addEventFilter(TouchEvent.TOUCH_MOVED, event -> {
            int touchId = event.getTouchPoint().getId();
            Node target = (Node) event.getTarget();
            Bounds bounds = target.localToScene(target.getBoundsInLocal());

            double x = event.getTouchPoint().getSceneX();
            double y = event.getTouchPoint().getSceneY();

            TouchDisplay circle = circleByTouchId.get(touchId);
            circle.relocate(x, y);

            Line line = lineByTouchId.get(touchId);
            line.setStartX(x);
            line.setStartY(y);
            line.setEndX(bounds.getMinX());
            line.setEndY(bounds.getMinY());
        });

        addEventFilter(TouchEvent.TOUCH_RELEASED, event -> {
            int touchId = event.getTouchPoint().getId();

            TouchDisplay circle = circleByTouchId.get(touchId);
            Line line = lineByTouchId.get(touchId);

            ScaleTransition st = new ScaleTransition(new Duration(200), circle);
            st.setFromX(1);
            st.setFromY(1);
            st.setToX(0);
            st.setToY(0);
            st.setOnFinished(e -> {
                overlay.getChildren().remove(circle);
            });

            FadeTransition ft = new FadeTransition(new Duration(100), line);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                overlay.getChildren().remove(line);
            });

            st.play();
            ft.play();
        });

        new AnimationTimer() {

            @Override
            public void handle(long arg0) {
                for (Node node : proximityDisplayByNode.keySet()) {
                    Bounds bounds = node.localToScene(node.getBoundsInLocal());
                    ProximityDisplay proximityDisplay = proximityDisplayByNode.get(node);
                    
                    proximityDisplay.setBoundsWidth(bounds.getWidth());
                    proximityDisplay.setBoundsHeight(bounds.getHeight());
                    proximityDisplay.relocate(bounds.getMinX(), bounds.getMinY());
                } 
                for (Node node : boundsDisplayByNode.keySet()) {
                    Bounds bounds = node.localToScene(node.getBoundsInLocal());
                    BoundsDisplay boundsDisplay = boundsDisplayByNode.get(node);
                    
                    boundsDisplay.setBoundsWidth(bounds.getWidth());
                    boundsDisplay.setBoundsHeight(bounds.getHeight());
                    boundsDisplay.relocate(bounds.getMinX(), bounds.getMinY());
                }
            }
        }.start();
    }
    
    // Returns a TouchPoint for a given MouseEvent
    private TouchPoint createTouchPoint(MouseEvent event) {
        TouchPoint tp = new TouchPoint(1, TouchPoint.State.PRESSED,
                event.getSceneX(), event.getSceneY(), event.getScreenX(), event.getScreenY(),
                event.getTarget(), null);
        touchPoints.clear();
        touchPoints.add(tp);
        return tp;
    }

    /**
     * Whether {@code MouseEvents} will be replaced with corresponding
     * {@code TouchEvents}
     *
     * @defaultValue false
     */
    private BooleanProperty mapMouseToTouch;

    public void setMapMouseToTouch(boolean value) {
        mapMouseToTouchProperty().set(value);
    }

    public boolean getMapMouseToTouch() {
        return mapMouseToTouchProperty().get();
    }

    public BooleanProperty mapMouseToTouchProperty() {
        if (mapMouseToTouch == null) {
            mapMouseToTouch = new SimpleBooleanProperty(false);
        }
        return mapMouseToTouch;
    }
    
    /**
     * Whether the overlay is visible or not
     * 
     * @defaultValue true
     */
    private BooleanProperty overlayVisible;

    public void setOverlayVisible(boolean value) {
        overlayVisibleProperty().set(value);
    }
    
    public boolean isOverlayVisible() {
        return overlayVisibleProperty().get();
    }
    
    public BooleanProperty overlayVisibleProperty() {
        if (overlayVisible == null) {
            overlayVisible = new SimpleBooleanProperty(true);
        }
        return overlayVisible;
    }
    
    /**
     * The radius of the circles that are drawn on touch events.
     */
    private DoubleProperty touchCircleRadius;

    public void setTouchCircleRadius(double value) {
        touchCircleRadiusProperty().set(value);
    }

    public double getTouchCircleRadius() {
        return touchCircleRadiusProperty().get();
    }

    public DoubleProperty touchCircleRadiusProperty() {
        if (touchCircleRadius == null) {
            touchCircleRadius = new SimpleDoubleProperty(50.0) {
                @Override
                public void set(double value) {
                    if (value < 0) {
                        value = 0;
                    }
                    super.set(value);
                }
            };
        }
        return touchCircleRadius;
    }
    
    public void clearTouchPoints() {
        List<Node> toRemove = new ArrayList<>();
        for (Node node : overlay.getChildren()) {
            if (node instanceof TouchDisplay)
                toRemove.add(node);
        }
        overlay.getChildren().removeAll(toRemove);
    }
    
    public void registerTactilePane(TactilePane pane) {
        for (Node node : pane.getChildren()) {
            registerDraggable(node);
        }
        
        pane.getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            c.next();
            for (Node node: c.getRemoved()) {
                deregisterDraggable(node);
            }
            for (Node node: c.getAddedSubList()) {
                registerDraggable(node);
            }
        });
        
        for (Node node : pane.getActiveNodes()) {
            registerActiveNode(node, pane);
        }
        
        pane.getActiveNodes().addListener((SetChangeListener.Change<? extends Node> c) -> {
            if (c.wasAdded()) {
                registerActiveNode(c.getElementAdded(), pane);
            } else {
                deregisterActiveNode(c.getElementRemoved());
            }
        });
    }
    
    private void registerActiveNode(Node node, TactilePane pane) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        
        ProximityDisplay pd = new ProximityDisplay(bounds.getWidth(), bounds.getHeight(), pane.proximityThresholdProperty());
        pd.relocate(bounds.getMinX(), bounds.getMinY());
        
        if (!boundsDisplayByNode.containsKey(node)) {
            BoundsDisplay bd = new BoundsDisplay(bounds.getWidth(), bounds.getHeight());
            boundsDisplayByNode.put(node, bd);
            bd.relocate(bounds.getMinX(), bounds.getMinY());
            overlay.getChildren().add(bd);
        }

        overlay.getChildren().add(pd);
        proximityDisplayByNode.put(node, pd);
    }
    
    private void deregisterActiveNode(Node node) {
        ProximityDisplay pd = proximityDisplayByNode.remove(node);
        overlay.getChildren().remove(pd);
        
        if (!(node.getParent() instanceof TactilePane)) {
            BoundsDisplay bd = boundsDisplayByNode.remove(node);
            overlay.getChildren().remove(bd);
        }
    }

    private void registerDraggable(Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        if (!boundsDisplayByNode.containsKey(node)) {
            BoundsDisplay bd = new BoundsDisplay(bounds.getWidth(), bounds.getHeight());
            boundsDisplayByNode.put(node, bd);
            bd.relocate(bounds.getMinX(), bounds.getMinY());
            overlay.getChildren().add(bd);
        }
    }

    private void deregisterDraggable(Node node) {
        if (TactilePane.getTracker(node) == null) {
            BoundsDisplay bd = boundsDisplayByNode.remove(node);
            overlay.getChildren().remove(bd);
        }

    }
}