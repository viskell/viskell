package nl.utwente.viskell.ui;

import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.TreeMap;

public class DebugParent extends StackPane {
    
    Pane overlay = new Pane();
    Map<Integer, TouchDisplay> circleByTouchId = new TreeMap<>();

    public DebugParent(Node node) {
        super(node);

        overlay.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));

        // Overlay shouldn't receive events
        overlay.setDisable(true);
        
        // Makes sure the overlay is always drawn on top of the other child
        getChildren().add(overlay);
        getChildren().addListener((Observable value) -> {
            overlay.toFront();
        });
        
        addEventFilter(TouchEvent.TOUCH_PRESSED, event -> {
            int touchId = event.getTouchPoint().getId();
            Node target = (Node) event.getTarget();
            Bounds bounds = target.localToScene(target.getBoundsInLocal());

            double x = event.getTouchPoint().getSceneX();
            double y = event.getTouchPoint().getSceneY();

            TouchDisplay circle = new TouchDisplay(x, y, bounds, touchId);
            circleByTouchId.put(touchId, circle);
            overlay.getChildren().add(circle);
            circle.relocate(x, y);
        });

        addEventFilter(TouchEvent.TOUCH_MOVED, event -> {
            int touchId = event.getTouchPoint().getId();
            Node target = (Node) event.getTarget();
            Bounds bounds = target.localToScene(target.getBoundsInLocal());

            double x = event.getTouchPoint().getX();
            double y = event.getTouchPoint().getY();

            TouchDisplay circle = circleByTouchId.get(touchId);
            circle.moveTouchPoint(x, y, bounds);
        });

        addEventFilter(TouchEvent.TOUCH_RELEASED, event -> {
            int touchId = event.getTouchPoint().getId();
            TouchDisplay circle = circleByTouchId.get(touchId);
            overlay.getChildren().remove(circle);
        });

    }
    
    public void setOverlayVisible(boolean value) {
        this.overlay.setVisible(value);
    }
   
}
