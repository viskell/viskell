package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.type.Type;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ValueTest {
    private final Type integer = Type.con("Integer");

    @Test
    public final void testToHaskell() throws HaskellException {
        final Expression v = new Value(this.integer, "10");
        assertEquals(this.integer.prettyPrint(), v.findType().prettyPrint());
        assertEquals("(10)", v.toHaskell());
    }
}
