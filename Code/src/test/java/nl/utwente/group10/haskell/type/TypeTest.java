package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class TypeTest {
    @Test
    public final void toHaskellTypeTest() {
        final Type t = new TupleT(
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
}
