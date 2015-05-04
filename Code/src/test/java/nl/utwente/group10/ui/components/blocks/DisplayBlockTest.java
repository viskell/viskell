package nl.utwente.group10.ui.components.blocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import nl.utwente.group10.ui.CustomUIPane;

import org.junit.Test;

public class DisplayBlockTest extends ComponentTest {
    @Test
    public void initTest() throws Exception {
        assertNotNull(new DisplayBlock(new CustomUIPane()));
    }

    @Test
    public void inputOutputTest() throws Exception {
        DisplayBlock block = new DisplayBlock(new CustomUIPane());
        assertEquals(block.getOutput(), "New Output");

        block.setOutput("8");
    }
}
