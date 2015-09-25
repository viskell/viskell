package nl.utwente.group10.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import nl.utwente.group10.ui.components.menu.GlobalMenu;

/**
 * ButtonOverlay provides an overlay with zoom buttons, toolbar buttons etc.
 */
public class ButtonOverlay extends StackPane {
    public static final Pos TOOLBAR_POS = Pos.TOP_LEFT;
    public static final Pos ZOOMBAR_POS = Pos.BOTTOM_CENTER;

    public static final String MENU_LABEL = "\u2630";

    public ButtonOverlay(Node child, CustomUIPane pane) {
        super();

        FlowPane toolBar = makeMenuBar();
        FlowPane buttons = makeZoomBar(pane);

        this.getChildren().setAll(child, buttons, toolBar);
    }

    private FlowPane makeMenuBar() {
        ContextMenu burgerMenu = new GlobalMenu();

        Button menu = new Button(MENU_LABEL);
        menu.setFocusTraversable(false);
        menu.setContextMenu(burgerMenu);
        menu.setOnAction(e -> menu.getContextMenu().show(menu, Side.BOTTOM, 0, 10));

        FlowPane toolBar = new FlowPane(10, 0, menu);
        toolBar.setMaxHeight(40);
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
}
