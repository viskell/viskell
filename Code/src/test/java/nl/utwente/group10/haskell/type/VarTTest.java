package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class VarTTest {
    @Test
    public final void toHaskellTypeTest() {
        final TypeVar v = new TypeVar("a");
        assertEquals("a", v.toHaskellType());
    }

    @Test
    public final void testCompareTo() {
        final TypeVar a = new TypeVar("a");
        final TypeVar b = new TypeVar("b", new TypeClass("Test"));

        assertEquals(0, a.compareTo(a));
        assertEquals(0, b.compareTo(b));

        assertNotEquals(0, a.compareTo(new TypeVar("a")));
        assertNotEquals(0, b.compareTo(new TypeVar("b")));
        assertNotEquals(0, b.compareTo(new TypeVar("b", new TypeClass("Test"))));
    }
}
