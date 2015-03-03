package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.ConstT;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValueTest {
    @Test
    public void testToHaskell() {
        Value v = new Value(new ConstT("Integer"), "10");
        assertEquals(v.toHaskell(), "10");
    }
}
