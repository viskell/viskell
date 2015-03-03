package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class ListTTest {
    @Test
    public void toHaskellTypeTest() {
        ConstT integer = new ConstT("Integer");
        ListT lt = new ListT(integer);
        assertEquals("[Integer]", lt.toHaskellType());
    }

    @Test
    public void testCompatibleWith() {
        ConstT integer = new ConstT("Integer");
        ConstT string = new ConstT("String");
        ListT lt = new ListT(integer);
        assertTrue(lt.compatibleWith(lt));
        assertTrue(lt.compatibleWith(new ListT(new ConstT("Integer"))));
        assertFalse(lt.compatibleWith(new ListT(string)));
        assertFalse(lt.compatibleWith(integer));
    }
}
