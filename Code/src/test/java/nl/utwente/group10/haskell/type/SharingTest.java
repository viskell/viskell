package nl.utwente.group10.haskell.type;

import org.junit.Test;

import static org.junit.Assert.*;

public class SharingTest {
    @Test
    public void testSharedSimple() throws Exception {
        Type x = Type.var("x");
        Type y = Type.var("y");
        Type u = Type.con("Unit");
        Type l = Type.listOf(x);

        assertTrue(x.toString().startsWith("x"));
        assertTrue(y.toString().startsWith("y"));
        assertEquals("Unit", u.toString());
        assertTrue(l.toString().startsWith("([] @ x"));

        TypeChecker.unify(x, y);

        assertEquals(x.toString(), y.toString());
        assertTrue(l.toString().startsWith("([] @ x"));

        TypeChecker.unify(x, u);

        assertEquals("Unit", x.toHaskellType());
        assertEquals("Unit", y.toHaskellType());
        assertEquals("[Unit]", l.toHaskellType());
    }
}
