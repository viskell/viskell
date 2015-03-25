package nl.utwente.group10.ui.componentTests;

import static org.junit.Assert.*;
import javafx.application.*;
import javafx.stage.Stage;

import java.io.IOException;

import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.FunctionBlock;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FunctionBlockTest {

	private FunctionBlock fblock1;
	private FunctionBlock fblock2;
	
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
	
	/**
	 * Before each test reset all the FunctionBlock instances
	 * to have a clear batch to test against.
	 * @throws IOException
	 */
	@Before
	public void setUp() throws IOException{
		fblock1 = FunctionBlock.newInstance(2);
		fblock2 = FunctionBlock.newInstance(4, "TestFunction1");
	}
	
	/**
	 * Test to see if instantiation of FunctionBlocks works
	 * for both instantiate variants.
	 */
	@Test
	public void instantiateTest() {
		assertNotNull(fblock1);
		assertNotNull(fblock2);
	}
	
	/**
	 * Test the setName and getName methods of FunctionBlock
	 */
	@Test
	public void nameTest() {
		fblock1.setName("testing again");
		assertEquals(fblock1.getName(), "testing again");
		fblock2.setName("name change");
		assertEquals(fblock2.getName(), "name change");
	}
	
	/**
	 * Test if all input anchors have been properly initialized
	 */
	@Test
	public void inputsTest() {
		assertNotNull(fblock1.getInputs());
		assertNotNull(fblock2.getInputs());
		assertEquals(fblock1.getInputs().length, 2);
		assertEquals(fblock2.getInputs().length, 4);
	}
	
	/**
	 * Test if arguments are initialized and if they can be found
	 * and changed.
	 */
	@Test
	public void argumentTest() {
		fblock1.setArgument(0, "test1");
		fblock2.setArgument(3, "test2");
		
		ConnectionAnchor[] anchors = fblock1.getInputs();
		ConnectionAnchor anchor = anchors[0];
		
		assertEquals(fblock1.getArgumentIndex(anchor), 0);
		assertEquals(fblock1.getArgument(anchor), "test1");
	}

}