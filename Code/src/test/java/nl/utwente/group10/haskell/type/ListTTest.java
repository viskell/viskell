package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ListTTest {
    @Test
    public final void toHaskellTypeTest() {
        final ConstT integer = new ConstT("Integer");
        final ListT list = new ListT(integer);
        assertEquals("[Integer]", list.toHaskellType());
    }
}
