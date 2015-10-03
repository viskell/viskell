package nl.utwente.group10.haskell.type;

import org.junit.Test;

import static org.junit.Assert.*;

public class SharingTest {
    @Test
    public void testSharedSimple() throws Exception {
        TypeVar x = new TypeVar("x");
        TypeVar y = new TypeVar("y");
        ConstT u = new ConstT("Unit");
        ListT l = new ListT(x);

        assertTrue(x.toString().startsWith("x"));
        assertTrue(y.toString().startsWith("y"));
        assertEquals("Unit", u.toString());
        assertTrue(l.toString().startsWith("([] x"));

        TypeChecker.unify(x, y);

        assertEquals(x.toString(), y.toString());
        assertTrue(l.toString().startsWith("([] x"));

        TypeChecker.unify(x, u);

        assertEquals("Unit", x.toHaskellType());
        assertEquals("Unit", y.toHaskellType());
        assertEquals("[Unit]", l.toHaskellType());
    }
}
