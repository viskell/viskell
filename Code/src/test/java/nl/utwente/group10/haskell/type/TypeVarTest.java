package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class TypeVarTest {
    @Test
    public final void toHaskellTypeTest() {
        final TypeVar v = new TypeVar("a");
        assertEquals("a", v.toHaskellType());
    }

    @Test
    public final void testCompareTo() {
        final TypeVar a = new TypeVar("a");
        final TypeVar b = new TypeVar("b", new TypeClass("Test"));

        assertEquals(a, a);
        assertEquals(b, b);

        assertNotEquals(a, new TypeVar("a"));
        assertNotEquals(b, new TypeVar("b"));
        assertNotEquals(b, new TypeVar("b", new TypeClass("Test")));
    }
}
