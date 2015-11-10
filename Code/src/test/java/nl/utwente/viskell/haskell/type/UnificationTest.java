package nl.utwente.viskell.haskell.type;

import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.haskell.expr.Apply;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Hole;
import nl.utwente.viskell.haskell.expr.Value;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnificationTest {

    @Test
    public void testUnifyUndefined() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();

        Expression e0 = env.useFun("const");
        Type t0 = e0.inferType();
        assertEquals("a -> b -> a", t0.prettyPrint());

        Expression e1 = new Apply(e0, new Hole());
        Type t1 = e1.inferType();
        assertEquals("b -> a", t1.prettyPrint());

        Expression e2 = new Apply(e1, new Hole());
        Type t2 = e2.inferType();

        TypeChecker.unify(e2, t2, new Hole().inferType());
        //No exception thrown -> Types are the same, as expected. The test will fail if an Exception is thrown.
    }

    @Test
    public void testUnifyFloats() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        Expression e0 = env.useFun("const");
        Type t0 = e0.inferType();
        assertEquals("a -> b -> a", t0.prettyPrint());

        Expression e1 = new Apply(e0, new Value(Type.con("Float"), "5.0"));
        Type t1 = e1.inferType();
        assertEquals("b -> Float", t1.prettyPrint());

        Expression e2 = new Apply(e1, new Value(Type.con("Float"), "5.0"));
        Type t2 = e2.inferType();
        assertEquals("Float", t2.prettyPrint());
    }
    
    @Test
    public void testUnifyABool() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();

        Expression e0 = env.useFun("const");
        e0.inferType();

        Expression e1 = new Apply(e0, new Hole());
        e1.inferType();

        Expression e2 = new Apply(e1, new Hole());
        Type t2 = e2.inferType();
        
        Type t3 = TypeScope.unique("t");
        Type t4 = Type.con("Bool");
        
        TypeChecker.unify(e1, t3, t4);
        
        TypeChecker.unify(e2, t2, t4);
    }

    @Test
    public void testTypeclassCopy() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        TypeClass num = env.testLookupClass("Num");
        TypeClass read = env.testLookupClass("Show");
        TypeClass show = env.testLookupClass("Read");

        TypeScope scope = new TypeScope();
        Type ct = Type.con("Float");
        Type t0 = scope.getVarTC("a", num, read);
        Type t1 = scope.getVarTC("b", num, show);

        Expression e0 = new Value(t0, "?");
        Expression e1 = new Value(t1, "?");
        Expression e2 = new Apply(new Apply(env.useFun("(+)"), e0), e1);

        e2.inferType();

        TypeChecker.unify(e0, t0, ct);
        TypeChecker.unify(e1, t1, ct);
    }
    
    @Test
    public void testInstanceChaining() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();

        // (id (id (1 :: Int)))
        Expression e0 = new Apply(env.useFun("id"),
            new Apply(env.useFun("id"),
                new Value(Type.con("Int"), "1")));

        Type t0 = e0.inferType();

        assertEquals(t0.prettyPrint(), "Int");
    }
    
    @Test
    public void testDeepUnification() throws HaskellException {
        TypeScope scope = new TypeScope();
        TypeVar a = scope.getVar("a");
        TypeVar b = scope.getVar("b");
        TypeVar x = scope.getVar("x");
        TypeVar y = scope.getVar("y");
        TypeChecker.unify("dummy", a, b);
        TypeChecker.unify("dummy", x, y);
        TypeChecker.unify("dummy", a, x);
        TypeChecker.unify("dummy", b, Type.con("Int"));
        assertEquals("Int", y.prettyPrint());
    }
}
