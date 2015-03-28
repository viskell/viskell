package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class VarTTest {
    @Test
    public final void toHaskellTypeTest() {
        final VarT v = new VarT("a");
        assertEquals("a", v.toHaskellType());
    }
}
