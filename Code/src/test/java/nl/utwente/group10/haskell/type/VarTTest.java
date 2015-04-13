package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VarTTest {
    @Test
    public final void toHaskellTypeTest() {
        final VarT v = new VarT("a");
        assertEquals("a", v.toHaskellType());
    }
}
