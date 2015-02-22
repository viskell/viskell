package nl.utwente.group10.haskell.type;

import nl.utwente.group10.ghcj.HaskellException;
import org.junit.Test;
import static org.junit.Assert.*;

public class FuncTTest {
    @Test
    public void toHaskellTypeTest() {
        ConstT integer = new ConstT("Integer");
        FuncT ft = new FuncT(integer, integer);
        assertEquals("(Integer -> Integer)", ft.toHaskellType());
    }

    @Test
    public void compatibleWithTest() {
        ConstT integer1 = new ConstT("Integer");
        ConstT integer2 = new ConstT("Integer");
        ConstT string = new ConstT("String");
        ConstT bool = new ConstT("Bool");
        TypeClass stringOrInteger = new TypeClass("StringOrInteger", new ConstT("Integer"), new ConstT("String"));
        FuncT ft = new FuncT(stringOrInteger, string);
        assertTrue(ft.compatibleWith(integer1));
        assertTrue(ft.compatibleWith(integer2));
        assertTrue(ft.compatibleWith(string));
        assertFalse(ft.compatibleWith(bool));
    }

    @Test
    public void getAppliedTypeTest() throws HaskellException {
        ConstT integer = new ConstT("Integer");
        ConstT string = new ConstT("String");
        ConstT bool = new ConstT("Bool");
        TypeClass stringOrInteger = new TypeClass("StringOrInteger", new ConstT("Integer"), new ConstT("String"));
        FuncT ft1 = new FuncT(integer, integer);
        FuncT ft2 = new FuncT(bool, ft1, stringOrInteger, string);
        assertEquals(ft2.getAppliedType(bool, ft1), new FuncT(stringOrInteger, string));
        try {
            ft2.getAppliedType(string);
            assertTrue(false);
        } catch (HaskellException e) {
            assertTrue(true);
        }
    }
}
