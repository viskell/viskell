package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.assertEquals;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;

import org.junit.Test;

public class ValueTest {
    private final Type integer = new ConstT("Integer");

    @Test
    public final void testToHaskell() throws HaskellException {
        final Expr v = new Value(this.integer, "10");
        assertEquals(this.integer.toHaskellType(), v.analyze(new Env()).toHaskellType());
        assertEquals("(10)", v.toHaskell());
    }
}
