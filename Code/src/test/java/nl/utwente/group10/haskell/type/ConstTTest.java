package nl.utwente.group10.haskell.type;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class ConstTTest {
    @Test
    public final void testToHaskellType() {
        final ConstT integer = new ConstT("Integer");
        final ConstT integerWithArg = new ConstT("Integer", integer);

        assertEquals("Integer", integer.toHaskellType());
        assertEquals("Integer Integer", integerWithArg.toHaskellType());
    }

    @Test
    public final void testCompareTo() {
        final ConstT integer = new ConstT("Integer");
        final ConstT weird = new ConstT("Weird", new ConstT("Integer"));

        assertEquals(0, integer.compareTo(integer));
        assertEquals(0, integer.compareTo(new ConstT("Integer")));
        assertNotEquals(0, integer.compareTo(new ConstT("Integer", new ConstT("Integer"))));

        assertEquals(0, weird.compareTo(weird));
        assertEquals(0, weird.compareTo(new ConstT("Weird", new ConstT("Integer"))));
        assertNotEquals(0, weird.compareTo(new ConstT("Weird")));
    }

    @Test
    public final void testFreshArgs() {
        final TypeVar staleVar = new TypeVar("a");
        final ConstT staleInt = new ConstT("Int");
        final ConstT stale = new ConstT("Type", staleInt, staleVar, staleVar);

        Type[] freshArgs = stale.getFreshArgs();

        assertEquals("[Int, a, a]", Arrays.toString(freshArgs));
        assertFalse(staleInt == freshArgs[0]);
        assertFalse(staleVar == freshArgs[1]);
        assertFalse(staleVar == freshArgs[2]);
        assertTrue(freshArgs[1] == freshArgs[2]);
    }
}
