package nl.utwente.group10.ui.components;

import static org.junit.Assert.*;

import java.io.IOException;

import nl.utwente.group10.ui.CustomUIPane;

import org.junit.Test;

public class DisplayBlockTest extends ComponentTest {
    @Test
    public void initTest() throws IOException {
        assertNotNull(new DisplayBlock(new CustomUIPane()));
    }

    @Test
    public void inputOutputTest() throws IOException {
        DisplayBlock block = new DisplayBlock(new CustomUIPane());
        assertEquals(block.getOutput(), "New Output");

        block.setOutput("8");
    }
}
