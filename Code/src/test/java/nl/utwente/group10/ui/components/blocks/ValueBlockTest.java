package nl.utwente.group10.ui.components.blocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import nl.utwente.group10.haskell.type.ConstT;
import org.junit.Test;

public class ValueBlockTest extends ComponentTest {
    @Test
    public void initTest() throws Exception {
        assertNotNull(new ValueBlock(getPane(), new ConstT("Float"), "0.0"));
    }

    @Test
    public void outputTest() throws Exception {
        ValueBlock block = new ValueBlock(getPane(), new ConstT("Float"), "0.0");
        block.setValue("6");
        assertEquals(block.getValue(), "6");
    }
}
