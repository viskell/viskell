package nl.utwente.viskell.ui;

import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nl.utwente.ewi.caes.tactilefx.DebugParent;
import nl.utwente.ewi.caes.tactilefx.TactilePane.EventProcessingMode;
import nl.utwente.viskell.ghcj.GhciEvaluator;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.env.HaskellCatalog;

/**
 * Main application class for the GUI.
 */
public class Main extends Application {
    /** The Width of the TactilePane used within Viskell. */
    public static final int PANE_WIDTH = 8000;
    
    /** The Height of the TactilePane used within Viskell. */
    public static final int PANE_HEIGHT = 4500;
    
    /** A reference to the main window */
    public static Stage primaryStage;
    
    /** A reference to the debug overlay */
    public static DebugParent debug;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        Font.loadFont(this.getClass().getResourceAsStream("/ui/fonts/titillium.otf"), 20);

        HaskellCatalog catalog = new HaskellCatalog();

        // Init TactilePane
        CustomUIPane tactilePane = new CustomUIPane(catalog);
        tactilePane.setMinWidth(PANE_WIDTH);
        tactilePane.setMinHeight(PANE_HEIGHT);
        tactilePane.setMaxWidth(PANE_WIDTH);
        tactilePane.setMaxHeight(PANE_HEIGHT);

        tactilePane.dragProcessingModeProperty().set(EventProcessingMode.HANDLER);

        // Init Debug
        debug = new DebugParent(tactilePane);
        debug.registerTactilePane(tactilePane);
        debug.setOverlayVisible(false);

        // Init zoom overlay
        ButtonOverlay buttonOverlay = new ButtonOverlay(debug, tactilePane);

        // Check if GHCI is available
        try {
            GhciSession testGhci = new GhciSession();
            testGhci.startAsync();
            testGhci.awaitRunning();
            testGhci.stopAsync();
        } catch (RuntimeException e) {
            String msg = "It seems the Glasgow Haskell Compiler, GHC, is not " +
                    "available. Executing programs will not be enabled. We " +
                    "strongly recommend you install GHC, for example by " +
                    "installing the Haskell Platform (haskell.org/platform).";
            new Alert(Alert.AlertType.WARNING, msg).showAndWait();

            e.printStackTrace(); // In case it's not a file-not-found
        }

        // Init scene
        Scene scene = new Scene(buttonOverlay);

        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String theme = prefs.get("theme", "/ui/colours.css");
        scene.getStylesheets().addAll("/ui/layout.css", theme);

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
