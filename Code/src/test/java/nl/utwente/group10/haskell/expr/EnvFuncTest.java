package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvFuncTest {
    @Test
    public void testToHaskell() {
        Type integer = new ConstT("Integer");
        FuncT integerToInteger = new FuncT(integer, integer);
        EnvFunc ef = new EnvFunc("(+)", integerToInteger);

        assertEquals(ef.toHaskell(), "(+)");
    }
}
