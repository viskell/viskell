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

public class ApplyTest {
    @Test
    public void testApplyPlus() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        Expression e0 = env.useFun("(+)");
        Type t0 = e0.findType();
        assertEquals("Num a -> Num a -> Num a", t0.prettyPrint());
        
        Expression e1 = new Value(Type.con("Float"), "5.0");
        Type t1 = e1.findType();
        assertEquals("Float", t1.prettyPrint());
        
        Expression e2 = new Apply(e0, e1);
        Type t2 = e2.findType();
        assertEquals("Float -> Float", t2.prettyPrint());
        
        Expression e3 = new Value(Type.con("Float"), "5.0");
        Type t3 = e3.findType();
        assertEquals("Float", t3.prettyPrint());
        
        Expression e4 = new Apply(e2, e3);
        Type t4 = e4.findType();
        assertEquals("Float", t4.prettyPrint());
    }
    
    @Test
    public void testApplyHoles() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        Expression e0 = env.useFun("(+)");
        Type t0 = e0.findType();
        assertEquals("Num a -> Num a -> Num a", t0.prettyPrint());
        
        Expression e1 = new Hole();
        Type t1 = e1.findType();
        TypeChecker.unify(e1, t1, Type.con("Float"));
        // t1 Should unify with everything (the type of t1 should be 'a').
        // No exception thrown -> Types are the same, as expected. The test will
        // fail if an Exception is thrown.
        assertEquals("Float", t1.prettyPrint());
        
        Expression e2 = new Apply(e0, e1);
        Type t2 = e2.findType();
        Type num = env.buildType("Num n => n");
        TypeChecker.unify(e2, t2, Type.fun(num, num));
        assertEquals("Float -> Float", t2.prettyPrint());
        
        Expression e3 = new Apply(e2, new Hole());
        Type t3 = e3.findType();
        TypeChecker.unify(e3, t3, Type.con("Float"));
        // t3 Should unify with everything (the type of t3 should be 'a').
        // No exception thrown -> Types are the same, as expected. The test will
        // fail if an Exception is thrown.
    }
}
