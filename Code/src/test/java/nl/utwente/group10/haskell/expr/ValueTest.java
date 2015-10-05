package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.assertEquals;
import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.Type;

import org.junit.Test;

public class ValueTest {
    private final Type integer = Type.con("Integer");

    @Test
    public final void testToHaskell() throws HaskellException {
        final Expression v = new Value(this.integer, "10");
        assertEquals(this.integer.toHaskellType(), v.analyze(new Environment()).toHaskellType());
        assertEquals("(10)", v.toHaskell());
    }
}
