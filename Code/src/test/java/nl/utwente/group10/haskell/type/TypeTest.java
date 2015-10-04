package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import nl.utwente.group10.haskell.exceptions.HaskellTypeError;

public class TypeTest {
    @Test
    public final void toHaskellTypeTest() {
        final Type t = Type.tupleOf(
                Type.listOf(Type.var("a")),
                Type.fun(
                        Type.var("b"),
                        Type.con("String")
                )
        );

        assertEquals("([a], b -> String)", t.toHaskellType());
    }

    @Test
    public final void getFreshTest() {
        final Type t = Type.tupleOf(
                Type.listOf(Type.var("a")),
                Type.fun(
                        Type.var("b"),
                        Type.con("String")
                )
        );

        assertFalse(t == t.getFresh());
        assertEquals(t.toHaskellType(), t.getFresh().toHaskellType());
    }

    @Test
    public final void nestedFreshTest() throws HaskellTypeError {
    	final TypeVar a = Type.var("a");
        final Type t = Type.tupleOf(Type.listOf(a), Type.listOf(a));                     
        final Type t2 = t.getFresh();
        
        assertEquals("([a], [a])", t.toHaskellType());
        assertEquals(t.toHaskellType(), t2.toHaskellType());

    	final TypeVar b = Type.var("b");
    	final Type i = Type.con("Int");
    	final Type t3 = Type.tupleOf(Type.listOf(i), Type.listOf(b));

    	TypeChecker.unify(t, t3);
    	assertEquals("([Int], [Int])", t.toHaskellType());

    	TypeChecker.unify(t2, t3);
    	assertEquals("([Int], [Int])", t2.toHaskellType());
    }
}
