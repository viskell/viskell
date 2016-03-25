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

    /** The current preferences window, or null if not yet opened. */
    private PreferencesWindow preferences;

    /** The current inspector window, or null if not yet opened. */
    private InspectorWindow inspector;

    /** The currently active customUIpane. */
    private ToplevelPane mainPane;
    
    public MainOverlay(ToplevelPane pane) {
        super();

        this.mainPane = pane;

        FlowPane buttons = makeZoomBar();

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

        StackPane stack = new StackPane();

        final String os = System.getProperty("os.name");
        boolean topMenu = (os != null && os.startsWith ("Mac"));
        stack.getChildren().setAll(this.mainPane, buttons, makeToolBars(topMenu), touchOverlay);
        this.setCenter(stack);
    }

    public ToplevelPane getMainPane() {
        return this.mainPane;
    }

    private FlowPane makeToolBars(boolean topMenu) {
        FlowPane toolBar;
        MenuActions menuActions = new MenuActions(this);

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

    private FlowPane makeZoomBar() {
        Button zoomIn = new Button("+");
        zoomIn.setOnAction(e -> this.zoom(1.1));

        Button zoomOut = new Button("–");
        zoomOut.setOnAction(e -> this.zoom(1/1.1));

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

    public void showPreferences() {
        if (this.preferences == null) {
            this.preferences = new PreferencesWindow(this);
        }

        this.preferences.show();
    }

    public void showInspector() {
        if (this.inspector == null) {
            this.inspector = new InspectorWindow(this);
        }

        this.inspector.show();
    }

    /**
     * Zooms the underlying main pane in/out with a ratio, up to reasonable limits. 
     * @param ratio the additional zoom factor to apply.
     */
    private void zoom(double ratio) {
        double scale = this.mainPane.getScaleX();

        /* Limit zoom to reasonable range. */
        if (scale <= 0.2 && ratio < 1) return;
        if (scale >= 3 && ratio > 1) return;

        this.mainPane.setScaleX(scale * ratio);
        this.mainPane.setScaleY(scale * ratio);
        this.mainPane.setTranslateX(this.mainPane.getTranslateX() * ratio);
        this.mainPane.setTranslateY(this.mainPane.getTranslateY() * ratio);
    }

}
