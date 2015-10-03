package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import nl.utwente.group10.haskell.exceptions.HaskellTypeError;

public class TypeTest {
    @Test
    public final void toHaskellTypeTest() {
        final Type t = new TupleT(
                new ListT(
                        new TypeVar("a")
                ),
                new FuncT(
                        new TypeVar("b"),
                        new ConstT("String")
                )
        );

        assertEquals("([a], b -> String)", t.toHaskellType());
    }

    @Test
    public final void getFreshTest() {
        final Type t = new TupleT(
                new ListT(
                        new TypeVar("a")
                ),
                new FuncT(
                        new TypeVar("b"),
                        new ConstT("String")
                )
        );

        assertFalse(t == t.getFresh());
        assertEquals(t.toHaskellType(), t.getFresh().toHaskellType());
    }

    @Test
    public final void nestedFreshTest() throws HaskellTypeError {
    	final TypeVar a = new TypeVar("a");
        final Type t = new TupleT(new ListT(a), new ListT(a));                     
        final Type t2 = t.getFresh();
        
        assertEquals("([a], [a])", t.toHaskellType());
        assertEquals(t.toHaskellType(), t2.toHaskellType());

    	final TypeVar b = new TypeVar("b");
    	final Type i = new ConstT("Int");
    	final Type t3 = new TupleT(new ListT(i), new ListT(b));

    	TypeChecker.unify(t, t3);
    	assertEquals("([Int], [Int])", t.toHaskellType());

    	TypeChecker.unify(t2, t3);
    	assertEquals("([Int], [Int])", t2.toHaskellType());
    }
    
}
