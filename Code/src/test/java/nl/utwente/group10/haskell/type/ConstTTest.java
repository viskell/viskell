package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import java.util.HashSet;

public class ConstTTest {
    @Test
    public final void testToHaskellType() {
        final ConstT integer = new ConstT("Integer");
        final ConstT integerWithArg = new ConstT("Integer", integer);

        assertEquals("Integer", integer.toHaskellType());
        assertEquals("Integer Integer", integerWithArg.toHaskellType());
    }

    @Test
    public final void testPrune() {
        final ConstT integer = new ConstT("Integer");
        final VarT a = new VarT("a", new HashSet<>(), integer);

        assertNotEquals(integer, a);
        assertEquals(integer, a.prune());
    }
}
