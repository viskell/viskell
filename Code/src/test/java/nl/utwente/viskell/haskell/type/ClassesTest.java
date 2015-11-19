package nl.utwente.viskell.haskell.type;

import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ClassesTest {
    @Test
    public final void testCompareTo() {
        TypeScope scope = new TypeScope();
        final TypeVar a = scope.getVar("a");
        final TypeVar b = scope.getVarTC("b", new TypeClass("Test"));

        assertEquals(a, a);
        assertEquals(b, b);
        
        final TypeVar a2 = scope.getVar("a");
        final TypeVar b2 = scope.getVar("b");
        assertEquals(a, a2);
        assertEquals(b, b2);

        TypeScope scope2 = new TypeScope(); 
        assertNotEquals(a, scope2.getVar("a"));
        assertNotEquals(b, scope2.getVar("b"));

        TypeScope scope3 = new TypeScope();
        assertNotEquals(b, scope3.getVarTC("b", new TypeClass("Test")));
    }
    
    @Test
    public final void testSuperClasses() throws HaskellTypeError {
        final Environment env = new HaskellCatalog().asEnvironment();
        final TypeClass eq = env.testLookupClass("Eq");
        final TypeClass ord = env.testLookupClass("Ord");
        final TypeClass integral = env.testLookupClass("Integral");

        TypeScope scope = new TypeScope();
        final TypeVar a = scope.getVarTC("a", eq);
        final TypeVar b = scope.getVarTC("b", ord);
        TypeChecker.unify("test", a, b);
        // the Eq constraint disappears because it is direct superclass of Ord 
        assertEquals("Ord b", a.prettyPrint());
        assertEquals("Ord b", b.prettyPrint());
        
        final TypeVar c = scope.getVarTC("c", integral);
        final TypeVar d = scope.getVarTC("d", ord);
        TypeChecker.unify("test", c, d);
        // indirectly through Real, Ord is also implied by Integral 
        assertEquals("Integral d", c.prettyPrint());
        assertEquals("Integral d", d.prettyPrint());

    }
    
    @Test
    public final void testNestedTypes() throws HaskellTypeError {
        final Environment env = new HaskellCatalog().asEnvironment();
        final TypeClass eq = env.testLookupClass("Eq");
        final TypeClass ord = env.testLookupClass("Ord");
        final TypeClass show = env.testLookupClass("Show");
        final TypeClass num = env.testLookupClass("Num");
        
        TypeScope scope = new TypeScope();
        final TypeVar a = scope.getVar("a");
        final TypeVar b = scope.getVar("b");
        final Type tab = Type.tupleOf(a, b ,a);
        final TypeVar x = scope.getVarTC("x", eq);
        TypeChecker.unify("test", tab, x);
        assertEquals("Eq a", a.prettyPrint());
        assertEquals("Eq b", b.prettyPrint());
        assertEquals("(Eq a, Eq b, Eq a)", x.prettyPrint());

       final TypeVar c = scope.getVarTC("c", num);
       final Type mc = Type.con("Maybe", c);
       final TypeVar y = scope.getVarTC("y", ord);
       TypeChecker.unify("test", y, mc);
       assertEquals("(Num+Ord c)", c.prettyPrint());
       assertEquals("Maybe (Num+Ord c)", y.prettyPrint());
       
       final Type str = Type.listOf(Type.con("Char"));
       final TypeVar z = scope.getVarTC("z", show);
       TypeChecker.unify("test", str, z);
       assertEquals("[Char]", z.prettyPrint());
    }
    
    @Test(expected = HaskellTypeError.class)
    public final void testUnsatisfiable() throws HaskellTypeError {
        final Environment env = new HaskellCatalog().asEnvironment();
        TypeScope scope = new TypeScope();
        TypeVar a = scope.getVarTC("a", env.testLookupClass("Integral"));
        TypeVar b = scope.getVarTC("b", env.testLookupClass("Fractional"));
        // throw error because, no type exist that is both instance of integral and fractional
        TypeChecker.unify("test", a, b);
    }
}
