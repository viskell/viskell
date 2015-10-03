package nl.utwente.group10.haskell.type;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SharingTest {
    @Test
    public void testSharedSimple() throws Exception {
        TypeVar x = new TypeVar("x");
        TypeVar y = new TypeVar("y");
        ConstT u = new ConstT("Unit");
        ListT l = new ListT(x);

        assertEquals("x", x.toString());
        assertEquals("y", y.toString());
        assertEquals("Unit", u.toString());
        assertEquals("([] x)", l.toString());

        TypeChecker.unify(x, y);

        assertEquals("x", x.toString());
        assertEquals("x", y.toString());
        assertEquals("([] x)", l.toString());

        TypeChecker.unify(x, u);

        assertEquals("Unit", x.toHaskellType());
        assertEquals("Unit", y.toHaskellType());
        assertEquals("[Unit]", l.toHaskellType());
    }
}
