package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.exceptions.HaskellException;
import org.junit.Test;
import static org.junit.Assert.*;

public class FuncTTest {
    @Test
    public final void toHaskellTypeTest() {
        final ConstT integer = new ConstT("Integer");
        final FuncT function = new FuncT(integer, integer);
        assertEquals("(Integer -> Integer)", function.toHaskellType());
    }
}
