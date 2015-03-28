package nl.utwente.group10.ui.components;

import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.BeforeClass;

/**
 * Superclass for component tests.
 */
public class ComponentTest {
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
}