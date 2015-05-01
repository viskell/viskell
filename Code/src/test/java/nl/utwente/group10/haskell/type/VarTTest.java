package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class VarTTest {
    @Test
    public final void toHaskellTypeTest() {
        final VarT v = new VarT("a");
        assertEquals("a", v.toHaskellType());
    }

    @Test
    public final void testCompareTo() {
        final VarT a = new VarT("a");
        final VarT b = new VarT("b", new TypeClass("Test"));

        assertEquals(0, a.compareTo(a));
        assertEquals(0, b.compareTo(b));

        assertNotEquals(0, a.compareTo(new VarT("a")));
        assertNotEquals(0, b.compareTo(new VarT("b")));
        assertNotEquals(0, b.compareTo(new VarT("b", new TypeClass("Test"))));
    }
}
