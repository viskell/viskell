package nl.utwente.group10.haskell.type;

import static org.junit.Assert.*;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.expr.Value;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.hindley.HindleyMilner;

import org.junit.Test;

public class ApplyTest {
    @Test
    public void testApplyPlus() throws CatalogException, HaskellException {
        Env env = new HaskellCatalog().asEnvironment();
        
        Expr e0 = new Ident("(+)");
        Type t0 = e0.analyze(env).prune();
        assertEquals("((Num a) -> ((Num a) -> (Num a)))", t0.toHaskellType());
        
        Expr e1 = new Value(new ConstT("Float"), "5.0");
        Type t1 = e1.analyze(env).prune();
        assertEquals("Float", t1.toHaskellType());
        
        Expr e2 = new Apply(e0, e1);
        Type t2 = e2.analyze(env).prune();
        assertEquals("(Float -> Float)", t2.toHaskellType());
        
        Expr e3 = new Value(new ConstT("Float"), "5.0");
        Type t3 = e3.analyze(env).prune();
        assertEquals("Float", t3.toHaskellType());
        
        Expr e4 = new Apply(e2, e3);
        Type t4 = e4.analyze(env).prune();
        assertEquals("Float", t4.toHaskellType());
    }
    
    @Test
    public void testApplyUndefined() throws CatalogException, HaskellException {
        Env env = new HaskellCatalog().asEnvironment();
        
        Expr e0 = new Ident("(+)");
        Type t0 = e0.analyze(env).prune();
        assertEquals("((Num a) -> ((Num a) -> (Num a)))", t0.toHaskellType());
        
        Expr e1 = new Ident("undefined");
        Type t1 = e1.analyze(env).prune();
        HindleyMilner.unify(t1, new Value(new ConstT("Float"), "5.0").analyze(env));
        // t1 Should unfiy with everything (the type of t1 should be 'a').
        // No exception thrown -> Types are the same, as expected. The test will
        // fail if an Exception is thrown.
        
        
        Expr e2 = new Apply(e0, e1);
        Type t2 = e2.analyze(env).prune();
        assertEquals("(a -> a)", t2.toHaskellType());
        
        Expr e3 = new Apply(e2, new Ident("undefined"));
        Type t3 = e3.analyze(env).prune();
        HindleyMilner.unify(t3, new Value(new ConstT("Float"), "5.0").analyze(env));
        // t3 Should unfiy with everything (the type of t3 should be 'a').
        // No exception thrown -> Types are the same, as expected. The test will
        // fail if an Exception is thrown.
    }
}
