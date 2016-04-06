package nl.utwente.viskell.ui.components;

import javafx.application.Application;
import javafx.stage.Stage;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ui.ToplevelPane;
import org.junit.BeforeClass;

/**
 * Superclass for component tests.
 */
public class ComponentIntegrationTest {
    private static Thread t = null;

    /**
     * Test Application extension with voided start method
     * to enable setup of unit testing.
     */
    public static class MockApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            //Do nothing
        }
    }

    /**
     * Before testing start a JavaFX thread to be able to run
     * and test JavaFX elements
     */
    @BeforeClass
    public static void initJFX() {
        if (t == null) {
            t = new Thread(() -> Application.launch(MockApp.class));
            t.setDaemon(true);
            t.start();
        }
    }

    protected ToplevelPane getPane() {
        GhciSession ghci = new GhciSession();
        ghci.startAsync();
        return new ToplevelPane(ghci);
    }
}
