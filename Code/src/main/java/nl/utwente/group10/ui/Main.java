package nl.utwente.group10.ui;

import com.google.common.io.Resources;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane.EventProcessingMode;
import nl.utwente.ewi.caes.tactilefx.debug.DebugParent;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;
import nl.utwente.group10.ghcj.GhciEvaluator;
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.ui.components.CustomAlert;
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
        Font.loadFont(this.getClass().getResourceAsStream("/ui/fonts/titillium.otf"), 20);

        tactilePane = FXMLLoader.load(this.getClass().getResource("/ui/Main.fxml"), null, new TactileBuilderFactory());

        tactilePane.dragProcessingModeProperty().set(EventProcessingMode.HANDLER);

        HaskellCatalog catalog = new HaskellCatalog();
        tactilePane.initialize(catalog);

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

        // Check if GHCI is available
        try {
            new GhciEvaluator().eval("");
        } catch (GhciException e) {
            String msg = "It seems the Glasgow Haskell Compiler, GHC, is not " +
                    "available. Executing programs will not be enabled. We " +
                    "strongly recommend you install GHC, for example by " +
                    "installing the Haskell Platform (haskell.org/platform).";
            tactilePane.getChildren().add(new CustomAlert(tactilePane, msg));

            e.printStackTrace(); // In case it's not a file-not-found
        }

        // Init scene
        Scene scene = new Scene(debug);

        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setScene(scene);

        stage.setMaximized(true);
        stage.show();

        valueBlock.relocate(tactilePane.getWidth() / 2, tactilePane.getHeight() / 2);
        displayBlock.relocate(tactilePane.getWidth() / 2, tactilePane.getHeight() / 2 + 100);

        // Invalidate
        tactilePane.invalidate();
        tactilePane.requestFocus();
    }

    /**
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
