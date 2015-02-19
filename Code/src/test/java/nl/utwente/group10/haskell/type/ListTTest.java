package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class ListTTest {
    @Test
    public void toStringTest() {
        ConstT integer = new ConstT("Integer");
        ListT lt = new ListT(integer);
        assertEquals("[Integer]", lt.toString());
    }
}
