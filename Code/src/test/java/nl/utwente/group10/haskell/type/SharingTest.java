package nl.utwente.group10.haskell.type;

import org.junit.Test;

import nl.utwente.group10.haskell.expr.Hole;

import static org.junit.Assert.*;

public class SharingTest {
    @Test
    public void testSharedSimple() throws Exception {
        TypeScope scope = new TypeScope();
        Type x = scope.getVar("x");
        Type y = scope.getVar("y");
        Type u = Type.con("Unit");
        Type l = Type.listOf(x);

        assertTrue(x.toString().startsWith("x"));
        assertTrue(y.toString().startsWith("y"));
        assertEquals("Unit", u.toString());
        assertTrue(l.toString().startsWith("([] @ x"));

        TypeChecker.unify(new Hole(), x, y);

        assertEquals(x.toString(), y.toString());
        assertTrue(l.toString().startsWith("([] @ y"));

        TypeChecker.unify(new Hole(), x, u);

        assertEquals("Unit", x.prettyPrint());
        assertEquals("Unit", y.prettyPrint());
        assertEquals("[Unit]", l.prettyPrint());
    }
}
