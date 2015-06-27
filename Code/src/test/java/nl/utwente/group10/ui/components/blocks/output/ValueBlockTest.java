package nl.utwente.group10.ui.components.blocks.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import nl.utwente.group10.ui.components.blocks.ComponentTest;

import org.junit.Test;

public class ValueBlockTest extends ComponentTest {
    @Test
    public void initTest() throws Exception {
        assertNotNull(new ValueBlock(getPane()));
    }

    @Test
    public void outputTest() throws Exception {
        ValueBlock block = new ValueBlock(getPane());
        block.setValue("6");
        assertEquals(block.getValue(), "6");
    }
}
