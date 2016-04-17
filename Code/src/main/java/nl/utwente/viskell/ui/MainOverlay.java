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
 * MainOverlay creates a stack of panes, the ToplevelPane with contents, menu button, zoom buttons, and touch overlay
 */
public class MainOverlay extends StackPane {
    public static final String MENU_LABEL = "\u2630";

    private final Pane touchOverlay;
    private final Map<Integer, TouchDisplay> circleByTouchId;
    private final ToplevelPane toplevelPane;

    public MainOverlay(ToplevelPane toplevelPane) {
        super();

        this.toplevelPane = toplevelPane;

        touchOverlay = makeTouchOverlay();
        circleByTouchId = new TreeMap<>();

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

        MenuActions menuActions = new MenuActions(this, toplevelPane);
        Pane zoomBar = makeZoomPane(menuActions);
        StackPane.setAlignment(zoomBar, Pos.BOTTOM_CENTER);

        getChildren().setAll(this.toplevelPane, zoomBar, getMenu(menuActions), touchOverlay);
    }

    /**
     * Create the menu to be shown. If we are running on an OS that can display a top menu, then create that
     * otherwise create an on-screen context menu
     *
     * @return menu to show
     */
    private Region getMenu(MenuActions menuActions) {
        Region menu;
        final String os = System.getProperty("os.name");
        if ((os != null) && os.startsWith("Mac")) {
            menu = new MacTopMenu(menuActions);
        } else {
            menu = makeMenuPane(menuActions);
            StackPane.setAlignment(menu, Pos.TOP_LEFT);
        }

        return menu;
    }

    /**
     * Get the ToplevelPane where all content is displayed
     *
     * @return ToplevelPane
     */
    public ToplevelPane getToplevelPane() {
        return this.toplevelPane;
    }

    /**
     * Create an invisible Pane that can be used to display touch events for debugging
     *
     * @return Touch Pane
     */
    private Pane makeTouchOverlay() {
        Pane touch = new Pane();
        // touch overlay shouldn't receive events
        touch.setDisable(true);
        // touch overlay is invisible by default
        touch.setVisible(false);

        return touch;
    }

    /**
     * Make a Pane that contains a Context menu that can be displayed on-screen
     *
     * @param menuActions that can be performed
     *
     * @return Menu Pane
     */
    private Pane makeMenuPane(MenuActions menuActions) {
        ContextMenu burgerMenu = new GlobalContextMenu(menuActions);
        Button menu = new Button(MENU_LABEL);
        menu.setFocusTraversable(false);
        menu.setContextMenu(burgerMenu);
        menu.setOnAction(e -> menu.getContextMenu().show(menu, Side.BOTTOM, 0, 10));

        FlowPane toolBar = new FlowPane(10, 0, menu);

        toolBar.setMaxHeight(40);
        toolBar.setMaxWidth(40); // workaround to make it not confiscate the whole top of the screen
        toolBar.setPadding(new Insets(10));
        toolBar.getStyleClass().add("overlayButtons");

        return toolBar;
    }

    /**
     * Make a Pane that contains buttons for zoom in and zoom out
     *
     * @param menuActions that can be performed, including Zoom In and Zoom Out actions
     *
     * @return Zoom Pane
     */
    private Pane makeZoomPane(MenuActions menuActions) {
        Button zoomIn = new Button("+");
        zoomIn.setOnAction(menuActions::zoomIn);

        Button zoomOut = new Button("–");
        zoomOut.setOnAction(menuActions::zoomOut);

        FlowPane zoomBar = new FlowPane(10, 0, zoomIn, zoomOut);
        zoomBar.setPrefSize(100, 20);
        zoomBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        zoomBar.setPadding(new Insets(10));
        zoomBar.getStyleClass().add("overlayButtons");
        return zoomBar;
    }

    /**
     * Set the touch overlay plane to be visible (so that touch events can be displayed) or invisible
     *
     * @param visible - new visibility state of the touch overlay plance
     */
    public void setTouchOverlayVisible(boolean visible) {
        this.touchOverlay.setVisible(visible);
    }
}
