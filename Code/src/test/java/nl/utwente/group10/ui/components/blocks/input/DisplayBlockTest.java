package nl.utwente.group10.ui.components.blocks.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import nl.utwente.group10.ui.components.blocks.ComponentTest;

import org.junit.Test;

public class DisplayBlockTest extends ComponentTest {
    @Test
    public void initTest() throws Exception {
        assertNotNull(new DisplayBlock(getPane()));
    }

    @Test
    public void inputOutputTest() throws Exception {
        DisplayBlock block = new DisplayBlock(getPane());
        assertEquals("???", block.getOutput());

        block.setOutput("8");
    }
}
