package nl.utwente.group10.haskell.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.env.HaskellCatalog;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;

public class TypeVarTest {
    @Test
    public final void toHaskellTypeTest() {
        final TypeVar v = Type.var("a");
        assertEquals("a", v.toHaskellType());
    }

    @Test
    public final void testCompareTo() {
        final TypeVar a = Type.var("a");
        final TypeVar b = Type.var("b", new TypeClass("Test"));

        assertEquals(a, a);
        assertEquals(b, b);

        assertNotEquals(a, Type.var("a"));
        assertNotEquals(b, Type.var("b"));
        assertNotEquals(b, Type.var("b", new TypeClass("Test")));
    }
    
    @Test
    public final void testSuperClasses() throws CatalogException, HaskellTypeError {
        final Environment env = new HaskellCatalog().asEnvironment();
        final TypeClass eq = env.lookupClass("Eq");
        final TypeClass ord = env.lookupClass("Ord");
        final TypeClass integral = env.lookupClass("Integral");

        final TypeVar a = Type.var("a", eq);
        final TypeVar b = Type.var("b", ord);
        TypeChecker.unify(a, b);
        // the Eq constraint disappears because it is direct superclass of Ord 
        assertEquals("(Ord a)", a.toHaskellType());
        assertEquals("(Ord a)", b.toHaskellType());
        
        final TypeVar c = Type.var("c", integral);
        final TypeVar d = Type.var("d", ord);
        TypeChecker.unify(c, d);
        // indirectly through Real, Ord is also implied by Integral 
        assertEquals("(Integral c)", c.toHaskellType());
        assertEquals("(Integral c)", d.toHaskellType());

    }
}
