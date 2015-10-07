package nl.utwente.group10.ui.components.blocks.function;

import static org.junit.Assert.*;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.env.FunctionInfo;
import nl.utwente.group10.ui.components.blocks.ComponentTest;

import org.junit.Before;
import org.junit.Test;

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

        functionBlock = new FunctionBlock(add, getPane());
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
