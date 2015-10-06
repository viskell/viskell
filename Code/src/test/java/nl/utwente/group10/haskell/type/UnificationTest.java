package nl.utwente.group10.haskell.type;

import static org.junit.Assert.*;

import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.env.HaskellCatalog;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expression;
import nl.utwente.group10.haskell.expr.Hole;
import nl.utwente.group10.haskell.expr.Value;

import org.junit.Test;

public class UnificationTest {

    @Test
    public void testUnifyUndefined() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();

        Expression e0 = env.useFun("const");
        Type t0 = e0.findType();
        assertEquals("a -> b -> a", t0.prettyPrint());

        Expression e1 = new Apply(e0, new Hole());
        Type t1 = e1.findType();
        assertEquals("b -> a", t1.prettyPrint());

        Expression e2 = new Apply(e1, new Hole());
        Type t2 = e2.findType();

        TypeChecker.unify(e2, t2, new Hole().findType());
        //No exception thrown -> Types are the same, as expected. The test will fail if an Exception is thrown.
    }

    @Test
    public void testUnifyFloats() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        Expression e0 = env.useFun("const");
        Type t0 = e0.findType();
        assertEquals("a -> b -> a", t0.prettyPrint());

        Expression e1 = new Apply(e0, new Value(Type.con("Float"), "5.0"));
        Type t1 = e1.findType();
        assertEquals("b -> Float", t1.prettyPrint());

        Expression e2 = new Apply(e1, new Value(Type.con("Float"), "5.0"));
        Type t2 = e2.findType();
        assertEquals("Float", t2.prettyPrint());
    }
    
    @Test
    public void testUnifyABool() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();

        Expression e0 = env.useFun("const");
        e0.findType();

        Expression e1 = new Apply(e0, new Hole());
        e1.findType();

        Expression e2 = new Apply(e1, new Hole());
        Type t2 = e2.findType();
        
        Type t3 = TypeScope.unique("t");
        Type t4 = Type.con("Bool");
        
        TypeChecker.unify(e1, t3, t4);
        
        TypeChecker.unify(e2, t2, t4);
    }

    @Test
    public void testTypeclassCopy() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        TypeClass num = env.lookupClass("Num");
        TypeClass read = env.lookupClass("Show");
        TypeClass show = env.lookupClass("Read");

        TypeScope scope = new TypeScope();
        Type ct = Type.con("Float");
        Type t0 = scope.getVarTC("a", num, read);
        Type t1 = scope.getVarTC("b", num, show);

        Expression e0 = new Value(t0, "?");
        Expression e1 = new Value(t1, "?");
        Expression e2 = new Apply(new Apply(env.useFun("(+)"), e0), e1);

        e2.findType();

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

        Type t0 = e0.findType();

        assertEquals(t0.prettyPrint(), "Int");
    }
}
