package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TupleTTest {
    @Test
    public final void toHaskellTypeTest() {
        final ConstT integer = new ConstT("Integer");
        final TupleT tuple = new TupleT(integer, integer);
        assertEquals("(Integer, Integer)", tuple.toHaskellType());
    }
}
