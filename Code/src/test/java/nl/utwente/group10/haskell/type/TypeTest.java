package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class TypeTest {
    @Test
    public void toHaskellTypeTest() {
        Type t = new TupleT(
                new ListT(
                        new VarT("a")
                ),
                new FuncT(
                        new VarT("b"),
                        new ConstT("String")
                )
        );

        assertEquals("([a], (b -> String))", t.toHaskellType());
    }

    @Test
    public void compareToTest() {
        Type t1 = new TupleT(new ConstT("Integer"), new ConstT("Integer"), new ConstT("Integer"));
        Type t2 = new ConstT("Integer");
        Type t3 = new ConstT("String");
        Type t4 = new VarT("a", new ConstT("Integer"), new ConstT("String"));

        assertTrue(t1.compareTo(t1) == 0);
        assertTrue(t2.compareTo(t2) == 0);
        assertTrue(t2.compareTo(t3) < 0);
        assertTrue(t3.compareTo(t2) > 0);
        assertTrue(t4.compareTo(t1) > 0);
        assertTrue(t4.compareTo(t2) > 0);
    }
}
