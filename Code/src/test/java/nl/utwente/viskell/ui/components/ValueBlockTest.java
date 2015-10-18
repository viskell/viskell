package nl.utwente.viskell.ui.components;

import nl.utwente.viskell.haskell.type.Type;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ValueBlockTest extends ComponentTest {
    @Test
    public void initTest() throws Exception {
        assertNotNull(new ValueBlock(getPane(), Type.con("Float"), "0.0"));
    }

    @Test
    public void outputTest() throws Exception {
        ValueBlock block = new ValueBlock(getPane(), Type.con("Float"), "0.0");
        block.setValue("6");
        assertEquals(block.getValue(), "6");
    }
}
