package nl.utwente.viskell.ui;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.*;

import java.util.Map;
import java.util.TreeMap;

/**
 * MainOverlay provides an overlay with zoom buttons, toolbar buttons etc.
 */
public class MainOverlay extends BorderPane {
    public static final Pos TOOLBAR_POS = Pos.TOP_LEFT;
    public static final Pos ZOOMBAR_POS = Pos.BOTTOM_CENTER;

    public static final String MENU_LABEL = "\u2630";

    private final Pane touchOverlay;
    final Map<Integer, TouchDisplay> circleByTouchId;

    private final ToplevelPane toplevelPane;
    
    public MainOverlay(ToplevelPane toplevelPane) {
        super();

        this.toplevelPane = toplevelPane;

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
            double x = event.getTouchPoint().getX();
            double y = event.getTouchPoint().getY();

            TouchDisplay circle = circleByTouchId.get(touchId);
            circle.moveTouchPoint(x, y);
        });

        addEventFilter(TouchEvent.TOUCH_RELEASED, event -> {
            int touchId = event.getTouchPoint().getId();
            TouchDisplay circle = circleByTouchId.get(touchId);
            touchOverlay.getChildren().remove(circle);
        });

        setOnScroll(event -> {
            if (!event.isInertia()) {
                setTranslateX(getTranslateX() + event.getDeltaX());
                setTranslateY(getTranslateY() + event.getDeltaY());
            }
            event.consume();
        });

        StackPane stack = new StackPane();
        MenuActions menuActions = new MenuActions(this);

        final String os = System.getProperty("os.name");
        boolean topMenu = (os != null && os.startsWith ("Mac"));
        stack.getChildren().setAll(this.toplevelPane, makeZoomBar(menuActions), makeToolBars(topMenu, menuActions), touchOverlay);
        this.setCenter(stack);
    }

    public ToplevelPane getToplevelPane() {
        return this.toplevelPane;
    }

    private FlowPane makeToolBars(boolean topMenu, MenuActions menuActions) {
        FlowPane toolBar;

        if (topMenu) {
            setTop(new MacTopMenu(menuActions));
            toolBar = new FlowPane(10, 0);
        } else {
            ContextMenu burgerMenu = new GlobalContextMenu(menuActions);
            Button menu = new Button(MENU_LABEL);
            menu.setFocusTraversable(false);
            menu.setContextMenu(burgerMenu);
            menu.setOnAction(e -> menu.getContextMenu().show(menu, Side.BOTTOM, 0, 10));

            toolBar = new FlowPane(10, 0, menu);
        }

        toolBar.setMaxHeight(40);
        toolBar.setMaxWidth(40); // workaround to make it not confiscate the whole top of the screen
        toolBar.setPadding(new Insets(10));
        toolBar.getStyleClass().add("overlayButtons");
        StackPane.setAlignment(toolBar, TOOLBAR_POS);

        return toolBar;
    }

    private FlowPane makeZoomBar(MenuActions menuActions) {
        Button zoomIn = new Button("+");
        zoomIn.setOnAction(menuActions::zoomIn);

        Button zoomOut = new Button("–");
        zoomOut.setOnAction(menuActions::zoomOut);

        FlowPane zoomBar = new FlowPane(10, 0, zoomIn, zoomOut);
        zoomBar.setPrefSize(100, 20);
        zoomBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        zoomBar.setPadding(new Insets(10));
        zoomBar.getStyleClass().add("overlayButtons");
        StackPane.setAlignment(zoomBar, ZOOMBAR_POS);
        return zoomBar;
    }
    
    public void setTouchVisible(boolean value) {
        this.touchOverlay.setVisible(value);
    }
}
