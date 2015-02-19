package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class FuncTTest {
    @Test
    public void toStringTest() {
        ConstT integer = new ConstT("Integer");
        FuncT ft = new FuncT(integer, integer);
        assertEquals("Integer -> Integer", ft.toString());
    }
}
