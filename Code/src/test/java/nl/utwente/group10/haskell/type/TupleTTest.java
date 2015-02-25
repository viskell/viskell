package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class TupleTTest {
    @Test
    public void toHaskellTypeTest() {
        ConstT integer = new ConstT("Integer");
        TupleT tt = new TupleT(integer, integer);
        assertEquals("(Integer, Integer)", tt.toHaskellType());
    }

    @Test
    public void testCompatibleWith() {
        ConstT integer = new ConstT("Integer");
        TupleT tt = new TupleT(integer, integer);
        assertTrue(tt.compatibleWith(tt));
        assertTrue(tt.compatibleWith(new TupleT(new ConstT("Integer"), new ConstT("Integer"))));
        assertFalse(tt.compatibleWith(new TupleT(new ConstT("Integer"), new ConstT("String"))));
        assertFalse(tt.compatibleWith(new TupleT(new ConstT("Integer"))));
    }
}
