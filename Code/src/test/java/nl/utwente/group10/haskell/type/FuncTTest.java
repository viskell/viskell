package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FuncTTest {
    @Test
    public final void toHaskellTypeTest() {
        final ConstT integer = new ConstT("Integer");
        final FuncT function = new FuncT(integer, integer);
        assertEquals("Integer -> Integer", function.toHaskellType());
    }
}
