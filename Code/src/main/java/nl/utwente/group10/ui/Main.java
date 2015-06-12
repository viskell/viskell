package nl.utwente.group10.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import nl.utwente.ewi.caes.tactilefx.control.TactilePane.EventProcessingMode;
import nl.utwente.ewi.caes.tactilefx.debug.DebugParent;
import nl.utwente.group10.ghcj.GhciEvaluator;
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.ui.components.CustomAlert;
import nl.utwente.group10.ui.components.blocks.DefinitionBlock;
import nl.utwente.group10.ui.components.blocks.DisplayBlock;
import nl.utwente.group10.ui.components.blocks.SliderBlock;
import nl.utwente.group10.ui.components.blocks.ValueBlock;

/**
 * Main application class for the GUI.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Font.loadFont(this.getClass().getResourceAsStream("/ui/fonts/titillium.otf"), 20);

        HaskellCatalog catalog = new HaskellCatalog();

        // Init TactilePane
        CustomUIPane tactilePane = new CustomUIPane(catalog);
        tactilePane.setBordersCollide(true);
        tactilePane.setMinWidth(3000);
        tactilePane.setMinHeight(3000);
        tactilePane.setMaxWidth(3000);
        tactilePane.setMaxHeight(3000);

        tactilePane.dragProcessingModeProperty().set(EventProcessingMode.HANDLER);

        ValueBlock valueBlock = new ValueBlock(tactilePane);
        DisplayBlock displayBlock = new DisplayBlock(tactilePane);
        DefinitionBlock definitionBlock = new DefinitionBlock(tactilePane);
        tactilePane.getChildren().addAll(valueBlock, displayBlock, definitionBlock);

        // Init Debug
        DebugParent debug = new DebugParent(tactilePane);
        debug.registerTactilePane(tactilePane);
        debug.setOverlayVisible(false);

        // Init zoom overlay
        ZoomOverlay zoomOverlay = new ZoomOverlay(debug, tactilePane);

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
        Scene scene = new Scene(zoomOverlay);
        scene.getStylesheets().add("/ui/style.css");

        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setScene(scene);

        stage.show();

        valueBlock.relocate(tactilePane.getWidth() / 2, tactilePane.getHeight() / 2);
        displayBlock.relocate(tactilePane.getWidth() / 2, tactilePane.getHeight() / 2 + 100);
        definitionBlock.relocate(tactilePane.getWidth() / 2, tactilePane.getHeight() / 2 + 200);

        // Invalidate
        tactilePane.invalidateAll();
        tactilePane.requestFocus();
    }

    /**
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
