package nl.utwente.viskell.haskell.type;

import nl.utwente.viskell.haskell.expr.Hole;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        TypeChecker.unify("test", x, y);

        assertEquals(x.toString(), y.toString());
        assertTrue(l.toString().startsWith("([] @ y"));

        TypeChecker.unify("test", x, u);

        assertEquals("Unit", x.prettyPrint());
        assertEquals("Unit", y.prettyPrint());
        assertEquals("[Unit]", l.prettyPrint());
    }
}
