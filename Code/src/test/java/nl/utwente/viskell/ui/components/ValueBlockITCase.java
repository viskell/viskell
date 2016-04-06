package nl.utwente.viskell.ui.components;

import nl.utwente.viskell.haskell.type.Type;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ValueBlockITCase extends ComponentIntegrationTest {
    @Test
    public void outputTest() throws Exception {
        ConstantBlock block = new ConstantBlock(getPane(), Type.con("Float"), "0.0", true);
        block.setValue("6");
        assertEquals(block.getValue(), "6");
    }
}
