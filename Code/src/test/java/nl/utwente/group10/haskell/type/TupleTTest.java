package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class TupleTTest {
    @Test
    public void toStringTest() {
        ConstT integer = new ConstT("Integer");
        TupleT tt = new TupleT(integer, integer);
        assertEquals("(Integer, Integer)", tt.toString());
    }
}
