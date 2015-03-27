package nl.utwente.group10.haskell.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class ListTTest {
    @Test
    public final void toHaskellTypeTest() {
        final ConstT integer = new ConstT("Integer");
        final ListT list = new ListT(integer);
        assertEquals("[Integer]", list.toHaskellType());
    }
}
