package nl.utwente.group10.haskell.type;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SharingTest {
    @Test
    public void testSharedSimple() throws Exception {
        VarT x = new VarT("x");
        VarT y = new VarT("y");
        ConstT u = new ConstT("Unit");
        ListT l = new ListT(x);

        assertEquals("x", x.toString());
        assertEquals("y", y.toString());
        assertEquals("Unit", u.toString());
        assertEquals("([] x)", l.toString());

        TypeChecker.unify(x, y);

        assertEquals("x:y", x.toString());
        assertEquals("y", y.toString());
        assertEquals("([] x:y)", l.toString());

        TypeChecker.unify(x, u);

        assertEquals("Unit", x.prune().toHaskellType());
        assertEquals("Unit", y.prune().toHaskellType());
        assertEquals("[Unit]", l.prune().toHaskellType());
    }
}
