package nl.utwente.viskell.ui.components;

import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FunctionBlockTest extends ComponentTest {

    private FunctionBlock functionBlock;

    /**
     * Before each test reset all the FunctionBlock instances
     * to have a clear batch to test against.
     */
    @Before
    public void setUp() throws Exception {
        Environment env = getPane().getEnvInstance();
        FunctionInfo add = env.lookupFun("(+)");

        functionBlock = new FunctionBlock(new LibraryFunUse(add), getPane());
    }

    /**
     * Test to see if instantiation of FunctionBlocks works
     * for both instantiate variants.
     */
    @Test
    public void instantiateTest() {
        assertNotNull(functionBlock);
    }

    /**
     * Test if all input anchors have been properly initialized
     */
    @Test
    public void inputsTest() {
        assertNotNull(functionBlock.getAllInputs());
        assertEquals(functionBlock.getAllInputs().size(), 2);
    }
}
