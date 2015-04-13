package nl.utwente.group10.ui.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import nl.utwente.group10.ui.CustomUIPane;

import org.junit.Test;

public class ValueBlockTest extends ComponentTest {
    @Test
    public void initTest() throws IOException {
        assertNotNull(new ValueBlock(new CustomUIPane()));
    }

    @Test
    public void outputTest() throws IOException {
        ValueBlock block = new ValueBlock(new CustomUIPane());
        block.setValue("6");
        assertEquals(block.getValue(), "6");
    }
}
