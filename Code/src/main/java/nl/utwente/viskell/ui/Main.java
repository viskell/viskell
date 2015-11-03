package nl.utwente.viskell.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane.EventProcessingMode;
import nl.utwente.ewi.caes.tactilefx.debug.DebugParent;
import nl.utwente.viskell.ghcj.GhciEvaluator;
import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.env.HaskellCatalog;

/**
 * Main application class for the GUI.
 */
public class Main extends Application {
    /** The Width of the TactilePane used within Viskell. */
    public static final int PANE_WIDTH = 8000;
    
    /** The Height of the TactilePane used within Viskell. */
    public static final int PANE_HEIGHT = 4500;

    @Override
    public void start(Stage stage) throws Exception {
        Font.loadFont(this.getClass().getResourceAsStream("/ui/fonts/titillium.otf"), 20);

        HaskellCatalog catalog = new HaskellCatalog();

        // Init TactilePane
        CustomUIPane tactilePane = new CustomUIPane(catalog);
        tactilePane.setBordersCollide(true);
        tactilePane.setMinWidth(PANE_WIDTH);
        tactilePane.setMinHeight(PANE_HEIGHT);
        tactilePane.setMaxWidth(PANE_WIDTH);
        tactilePane.setMaxHeight(PANE_HEIGHT);

        tactilePane.dragProcessingModeProperty().set(EventProcessingMode.HANDLER);

        // Init Debug
        DebugParent debug = new DebugParent(tactilePane);
        debug.registerTactilePane(tactilePane);
        debug.setOverlayVisible(false);

        // Init zoom overlay
        ButtonOverlay buttonOverlay = new ButtonOverlay(debug, tactilePane);

        // Check if GHCI is available
        try {
            GhciEvaluator testGhci = new GhciEvaluator(); 
            testGhci.eval("");
            testGhci.close();
        } catch (HaskellException e) {
            String msg = "It seems the Glasgow Haskell Compiler, GHC, is not " +
                    "available. Executing programs will not be enabled. We " +
                    "strongly recommend you install GHC, for example by " +
                    "installing the Haskell Platform (haskell.org/platform).";
            CustomAlert alert = new CustomAlert(tactilePane, msg);
            tactilePane.getChildren().add(alert);
            alert.relocate(tactilePane.getWidth() / 2 - 200, tactilePane.getHeight() / 2 - 200);

            e.printStackTrace(); // In case it's not a file-not-found
        }
        
        // Init scene
        Scene scene = new Scene(buttonOverlay);
        scene.getStylesheets().add("/ui/style.css");

        stage.setWidth(1024);
        stage.setHeight(768);

        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setScene(scene);

        stage.show();

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
