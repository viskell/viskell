package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class VarTTest {
    @Test
    public void toHaskellTypeTest() {
        VarT v = new VarT("a");
        assertEquals("a", v.toHaskellType());
    }

    @Test
    public void compatibleWithTest() {
        VarT v = new VarT("a", new ConstT("String"), new ConstT("Integer"));
        assertTrue(v.compatibleWith(new ConstT("String")));
        assertTrue(v.compatibleWith(new ConstT("Integer")));
        assertFalse(v.compatibleWith(new ConstT("Bool")));
    }
}
