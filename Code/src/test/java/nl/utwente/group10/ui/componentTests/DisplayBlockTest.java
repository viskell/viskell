package nl.utwente.group10.ui.componentTests;

import static org.junit.Assert.*;
import javafx.application.*;
import javafx.stage.Stage;

import java.io.IOException;

import nl.utwente.group10.ui.components.DisplayBlock;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DisplayBlockTest {

    /**
     * Test Application extension with voided start method
     * to enable setup of unit testing.
     */
    public static class JTestApp extends Application {
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
        Thread t = new Thread("JavaFX Init Thread") {
            public void run() {
                Application.launch(JTestApp.class, new String[0]);
            }
        };
        t.setDaemon(true);
        t.start();
    }

    @Test
    public void initTest() throws IOException {
        assertNotNull(DisplayBlock.newInstance());
    }

    @Test
    public void inputOutputTest() throws IOException {
        DisplayBlock block = DisplayBlock.newInstance();
        assertNull(block.getOutput());

        block.setInput("8");
        assertEquals(block.getOutput(), "8");
    }
}
