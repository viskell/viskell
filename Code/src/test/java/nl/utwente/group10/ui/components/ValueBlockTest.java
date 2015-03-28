package nl.utwente.group10.ui.components;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ValueBlockTest extends ComponentTest {
    @Test
    public void initTest() throws IOException {
        assertNotNull(new ValueBlock("6"));
    }

    @Test
    public void outputTest() throws IOException {
        ValueBlock block = new ValueBlock("6");
        assertEquals(block.getValue(), "6");
    }
}
