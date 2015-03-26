package nl.utwente.group10.ui.components;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ValueBlockTest {
    @Test
    public void initTest() throws IOException {
        assertNotNull(ValueBlock.newInstance("6"));
    }

    @Test
    public void outputTest() throws IOException {
        ValueBlock block = ValueBlock.newInstance("6");
        assertEquals(block.getOutput(), "6");
    }
}
