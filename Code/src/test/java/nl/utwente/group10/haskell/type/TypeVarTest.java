package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class TypeVarTest {
    @Test
    public final void toHaskellTypeTest() {
        final TypeVar v = Type.var("a");
        assertEquals("a", v.toHaskellType());
    }

    @Test
    public final void testCompareTo() {
        final TypeVar a = Type.var("a");
        final TypeVar b = Type.var("b", new TypeClass("Test"));

        assertEquals(a, a);
        assertEquals(b, b);

        assertNotEquals(a, Type.var("a"));
        assertNotEquals(b, Type.var("b"));
        assertNotEquals(b, Type.var("b", new TypeClass("Test")));
    }
}
