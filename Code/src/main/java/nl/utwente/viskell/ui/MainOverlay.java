package nl.utwente.viskell.ui;

import java.util.Map;
import java.util.TreeMap;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * MainOverlay provides an overlay with zoom buttons, toolbar buttons etc.
 */
public class MainOverlay extends StackPane {
    public static final Pos TOOLBAR_POS = Pos.TOP_LEFT;
    public static final Pos ZOOMBAR_POS = Pos.BOTTOM_CENTER;

    public static final String MENU_LABEL = "\u2630";

    private final Pane touchOverlay;
    final Map<Integer, TouchDisplay> circleByTouchId;
    
    public MainOverlay(CustomUIPane pane) {
        super();

        FlowPane toolBar = makeMenuBar(pane);
        FlowPane buttons = makeZoomBar(pane);

        circleByTouchId = new TreeMap<>();
        touchOverlay = new Pane();
        // touch overlay shouldn't receive events
        touchOverlay.setDisable(true);
        // touch overlay is invisible by default
        this.touchOverlay.setVisible(false);
        
        addEventFilter(TouchEvent.TOUCH_PRESSED, event -> {
            int touchId = event.getTouchPoint().getId();
            Node target = (Node) event.getTarget();
            Bounds bounds = target.localToScene(target.getBoundsInLocal());

            double x = event.getTouchPoint().getSceneX();
            double y = event.getTouchPoint().getSceneY();

            TouchDisplay circle = new TouchDisplay(x, y, bounds, touchId);
            circleByTouchId.put(touchId, circle);
            touchOverlay.getChildren().add(circle);
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
            touchOverlay.getChildren().remove(circle);
        });
                
        this.getChildren().setAll(pane, buttons, toolBar, touchOverlay);
    }

    private FlowPane makeMenuBar(CustomUIPane pane) {
        ContextMenu burgerMenu = new GlobalMenu(pane);

        Button menu = new Button(MENU_LABEL);
        menu.setFocusTraversable(false);
        menu.setContextMenu(burgerMenu);
        menu.setOnAction(e -> menu.getContextMenu().show(menu, Side.BOTTOM, 0, 10));

        FlowPane toolBar = new FlowPane(10, 0, menu);
        toolBar.setMaxHeight(40);
        toolBar.setMaxWidth(40); // workaround to make it not confiscate the whole top of the screen
        toolBar.setPadding(new Insets(10));
        toolBar.getStyleClass().add("overlayButtons");
        StackPane.setAlignment(toolBar, TOOLBAR_POS);

        return toolBar;
    }

    private FlowPane makeZoomBar(CustomUIPane pane) {
        Button zoomIn = new Button("+");
        zoomIn.setOnAction(e -> pane.zoomIn());

        Button zoomOut = new Button("–");
        zoomOut.setOnAction(e -> pane.zoomOut());

        FlowPane buttons = new FlowPane(10, 0, zoomIn, zoomOut);
        buttons.setPrefSize(100, 20);
        buttons.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        buttons.setPadding(new Insets(10));
        buttons.getStyleClass().add("overlayButtons");
        StackPane.setAlignment(buttons, ZOOMBAR_POS);
        return buttons;
    }
    
    public void setTouchVisible(boolean value) {
        this.touchOverlay.setVisible(value);
    }

}
