package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class TypeClassTest {
    @Test
    public void toHaskellTypeTest() {
        TypeClass tc = new TypeClass("BasicNum", new ConstT("Integer"), new ConstT("Floating"));

        assertEquals("instance BasicNum Integer\ninstance BasicNum Floating", tc.toHaskellType());
    }

    @Test
    public void compatibleWithTest() {
        TypeClass tc = new TypeClass("BasicNum", new ConstT("Integer"), new ConstT("Floating"));

        assertTrue(tc.compatibleWith(new ConstT("Integer")));
        assertTrue(tc.compatibleWith(new ConstT("Floating")));
        assertFalse(tc.compatibleWith(new ConstT("String")));
        assertFalse(tc.compatibleWith(new TypeClass("Integer", new ConstT("Integer"))));
        assertFalse(tc.compatibleWith(new VarT("Integer")));
    }
}
