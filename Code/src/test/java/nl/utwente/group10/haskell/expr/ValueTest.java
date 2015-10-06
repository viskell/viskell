package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.assertEquals;

import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.type.Type;

import org.junit.Test;

public class ValueTest {
    private final Type integer = Type.con("Integer");

    @Test
    public final void testToHaskell() throws HaskellException {
        final Expression v = new Value(this.integer, "10");
        assertEquals(this.integer.prettyPrint(), v.findType().prettyPrint());
        assertEquals("(10)", v.toHaskell());
    }
}
