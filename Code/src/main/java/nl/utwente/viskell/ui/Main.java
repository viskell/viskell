package nl.utwente.viskell.ui;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ghcj.HaskellException;

import java.util.prefs.Preferences;

/**
 * Main application class for the GUI.
 */
public class Main extends Application {
    /** A reference to the main window */
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Font.loadFont(this.getClass().getResourceAsStream("/ui/fonts/titillium.otf"), 20);

        GhciSession ghci = new GhciSession();
        ghci.startAsync();
        
        // Init TactilePane
        ToplevelPane tactilePane = new ToplevelPane(ghci);
        MainOverlay overlay = new MainOverlay(tactilePane);
        Scene scene = new Scene(overlay);
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String backGroundImage = prefs.get("background", "/ui/grid.png");
        overlay.getMainPane().setStyle("-fx-background-image: url('" + backGroundImage + "');");
        String theme = prefs.get("theme", "/ui/colours.css");
        scene.getStylesheets().addAll("/ui/layout.css", theme);

        stage.setWidth(1024);
        stage.setHeight(768);

        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setScene(scene);

        stage.show();
        tactilePane.requestFocus();
        
        // Check if GHCI is available
        try {
            ghci.awaitRunning();
            // trigger loading of libraries and test QuickCheck
            ListenableFuture<String> test = ghci.pullRaw("sample' (arbitrary :: Gen Int)");
            Futures.addCallback(test, new FutureCallback<String>() {
                public void onSuccess(String result) {
                    // all ok
                }
                public void onFailure(Throwable error) {
                    error.printStackTrace();
                    String msg = "It seems like QuickCheck is not working, thus the Arbitry block can not be used.";
                    if (error instanceof HaskellException) {
                        Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, msg).showAndWait());
                    }
                }
            });
        } catch (RuntimeException e) {
            String msg = "It seems the Glasgow Haskell Compiler, GHC, is not " +
                    "available. Executing programs will not be enabled. We " +
                    "strongly recommend you install GHC, for example by " +
                    "installing the Haskell Platform (haskell.org/platform)." +
                    "Or it might be that the QuickCheck is not installed.";
            new Alert(Alert.AlertType.WARNING, msg).showAndWait();

            e.printStackTrace(); // In case it's not a file-not-found
        }
    }

    /**
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
