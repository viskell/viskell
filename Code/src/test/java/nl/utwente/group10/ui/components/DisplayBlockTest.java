package nl.utwente.group10.ui.components;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class DisplayBlockTest extends ComponentTest {
    @Test
    public void initTest() throws IOException {
        assertNotNull(DisplayBlock.newInstance());
    }

    @Test
    public void inputOutputTest() throws IOException {
        DisplayBlock block = DisplayBlock.newInstance();
        assertNull(block.getOutput());

        block.setInput("8");
        assertEquals(block.getOutput(), "8");
    }
}
