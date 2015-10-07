package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FunTypeTest {
    @Test
    public final void toHaskellTypeTest() {
        final Type integer = Type.con("Integer");
        final Type function = Type.fun(integer, integer);
        assertEquals("Integer -> Integer", function.prettyPrint());
    }
}
