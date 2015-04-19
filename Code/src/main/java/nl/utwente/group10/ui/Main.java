package nl.utwente.group10.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane.EventProcessingMode;
import nl.utwente.ewi.caes.tactilefx.debug.DebugParent;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.ui.components.blocks.DisplayBlock;
import nl.utwente.group10.ui.components.blocks.ValueBlock;

/**
 * Main application class for the GUI.
 */
public class Main extends Application {
    /** Pane that is used for outputting debug information about touch interactions and user interface elements. */
    private DebugParent debug;

    /** Primary pane which contains blocks and connections. */
    private CustomUIPane tactilePane;

    @Override
    public void start(Stage stage) throws Exception {
        tactilePane = FXMLLoader.load(this.getClass().getResource("/ui/Main.fxml"), null, new TactileBuilderFactory());

        tactilePane.dragProcessingModeProperty().set(EventProcessingMode.HANDLER);

        HaskellCatalog catalog = new HaskellCatalog();

        ValueBlock valueBlock = new ValueBlock(tactilePane);
        DisplayBlock displayBlock = new DisplayBlock(tactilePane);
        tactilePane.getChildren().addAll(valueBlock, displayBlock);

        // Init Debug
        debug = new DebugParent(tactilePane);
        debug.registerTactilePane(tactilePane);
        debug.setOverlayVisible(false);

        // Init menu
        ContextMenu menu = new MainMenu(catalog, tactilePane);
        tactilePane.setContextMenu(menu);

        // Init scene
        Scene scene = new Scene(debug);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKey);

        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setScene(scene);

        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();

        valueBlock.relocate(tactilePane.getWidth() / 2, tactilePane.getHeight() / 2);
        displayBlock.relocate(tactilePane.getWidth() / 2, tactilePane.getHeight() / 2 + 100);

        // Invalidate
        tactilePane.invalidate();
    }

    private void handleKey(KeyEvent event) {
        switch (event.getCode()) {
            case DELETE:
                tactilePane.removeSelected();
                break;
        }
    }

    /**
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
