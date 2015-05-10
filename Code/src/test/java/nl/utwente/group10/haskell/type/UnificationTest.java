package nl.utwente.group10.haskell.type;

import static org.junit.Assert.*;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.expr.Value;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.hindley.HindleyMilner;

import org.junit.Test;

public class UnificationTest {

    @Test
    public void testUnifyUndefined() throws CatalogException, HaskellException {
        Env env = new HaskellCatalog().asEnvironment();

        Expr e0 = new Ident("const");
        Type t0 = e0.analyze(env).prune();
        assertEquals("(a -> (b -> a))", t0.toHaskellType());

        Expr e1 = new Apply(e0, new Ident("undefined"));
        Type t1 = e1.analyze(env).prune();
        assertEquals("(b -> a)", t1.toHaskellType());

        Expr e2 = new Apply(e1, new Ident("undefined"));
        Type t2 = e2.analyze(env).prune();

        HindleyMilner.unify(t2, new Ident("undefined").analyze(env));
        //No exception thrown -> Types are the same, as expected. The test will fail if an Exception is thrown.
    }

    @Test
    public void testUnifyFloats() throws CatalogException, HaskellException {
        Env env = new HaskellCatalog().asEnvironment();
        
        Expr e0 = new Ident("const");
        Type t0 = e0.analyze(env).prune();
        assertEquals("(a -> (b -> a))", t0.toHaskellType());

        Expr e1 = new Apply(e0, new Value(new ConstT("Float"), "5.0"));
        Type t1 = e1.analyze(env).prune();
        assertEquals("(b -> Float)", t1.toHaskellType());

        Expr e2 = new Apply(e1, new Value(new ConstT("Float"), "5.0"));
        Type t2 = e2.analyze(env).prune();
        assertEquals("Float", t2.toHaskellType());
    }
    
    @Test
    public void testUnifyABool() throws HaskellException{
        Env env = new HaskellCatalog().asEnvironment();

        Expr e0 = new Ident("const");
        Type t0 = e0.analyze(env).prune();

        Expr e1 = new Apply(e0, new Ident("undefined"));
        Type t1 = e1.analyze(env).prune();

        Expr e2 = new Apply(e1, new Ident("undefined"));
        Type t2 = e2.analyze(env).prune();
        
        Type t3 = HindleyMilner.makeVariable();
        Type t4 = new ConstT("Bool");
        
        HindleyMilner.unify(t3, t4);
        
        HindleyMilner.unify(t2, t4);
    }
}
