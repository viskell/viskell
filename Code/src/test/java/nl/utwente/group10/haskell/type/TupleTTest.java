package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class TupleTTest {
    @Test
    public final void toHaskellTypeTest() {
        final ConstT integer = new ConstT("Integer");
        final TupleT tuple = new TupleT(integer, integer);
        assertEquals("(Integer, Integer)", tuple.toHaskellType());
    }
}
