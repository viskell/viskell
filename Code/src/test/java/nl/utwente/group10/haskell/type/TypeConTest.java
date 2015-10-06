package nl.utwente.group10.haskell.type;

import org.junit.Test;

import static org.junit.Assert.*;

public class TypeConTest {
    @Test
    public final void testToHaskellType() {
        final Type integer = Type.con("Integer");
        final Type integerWithArg = Type.con("Integer", integer);

        assertEquals("Integer", integer.prettyPrint());
        assertEquals("Integer Integer", integerWithArg.prettyPrint());
    }

    @Test
    public final void testFreshArgs() {
        final Type staleVar = TypeScope.unique("a");
        final Type staleInt = Type.con("Int");
        final Type stale = Type.con("Type", staleInt, staleVar, staleVar);

        TypeApp fresh = (TypeApp)stale.getFresh();
        
        Type arg2 = fresh.getTypeArg();
        TypeApp ta = (TypeApp) fresh.getTypeFun();
        Type arg1 = ta.getTypeArg();
        TypeApp tb = (TypeApp) ta.getTypeFun();
        Type arg0 = tb.getTypeArg();

        assertEquals("Int", arg0.toString());
        assertTrue(arg1.toString().startsWith("a"));
        assertTrue(arg2.toString().startsWith("a"));
        assertTrue(staleInt == arg0);
        assertFalse(staleVar == arg1);
        assertFalse(staleVar == arg2);
        assertTrue(arg1 == arg2);
    }
    
    @Test
    public final void toHaskellListTest() {
        final Type ilist = Type.listOf(Type.con("Integer"));
        assertEquals("[Integer]", ilist.prettyPrint());
    }

    @Test
    public final void toHaskellTupleTest() {
        final Type integer = Type.con("Integer");
        final Type tuple = Type.tupleOf(integer, integer);
        assertEquals("(Integer, Integer)", tuple.prettyPrint());
    }

}
