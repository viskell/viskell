package nl.utwente.group10.ui.components.blocks.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
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
        //TODO: not entirely correct.
        FuncT func = new FuncT(new ConstT("Int"), new FuncT(new ConstT("Int"), new ConstT("Int")));

        functionBlock = new FunctionBlock("(+)", func, getPane());
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
     * Test the setName and getName methods of FunctionBlock
     */
    @Test
    public void nameTest() {
        functionBlock.setName("name change");
        assertEquals(functionBlock.getName(), "name change");
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
