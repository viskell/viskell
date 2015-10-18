package nl.utwente.viskell.ui.components;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
