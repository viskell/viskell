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

import org.junit.Test;

public class UnificationTest {

    @Test
    public void testUnifyUndefined() throws CatalogException, HaskellException {
        Env env = new HaskellCatalog().asEnvironment();

        Expr e0 = new Ident("const");
        Type t0 = e0.analyze(env);
        assertEquals("a -> b -> a", t0.toHaskellType());

        Expr e1 = new Apply(e0, new Ident("undefined"));
        Type t1 = e1.analyze(env);
        assertEquals("b -> a", t1.toHaskellType());

        Expr e2 = new Apply(e1, new Ident("undefined"));
        Type t2 = e2.analyze(env);

        TypeChecker.unify(t2, new Ident("undefined").analyze(env));
        //No exception thrown -> Types are the same, as expected. The test will fail if an Exception is thrown.
    }

    @Test
    public void testUnifyFloats() throws CatalogException, HaskellException {
        Env env = new HaskellCatalog().asEnvironment();
        
        Expr e0 = new Ident("const");
        Type t0 = e0.analyze(env);
        assertEquals("a -> b -> a", t0.toHaskellType());

        Expr e1 = new Apply(e0, new Value(new ConstT("Float"), "5.0"));
        Type t1 = e1.analyze(env);
        assertEquals("b -> Float", t1.toHaskellType());

        Expr e2 = new Apply(e1, new Value(new ConstT("Float"), "5.0"));
        Type t2 = e2.analyze(env);
        assertEquals("Float", t2.toHaskellType());
    }
    
    @Test
    public void testUnifyABool() throws HaskellException{
        Env env = new HaskellCatalog().asEnvironment();

        Expr e0 = new Ident("const");
        Type t0 = e0.analyze(env);

        Expr e1 = new Apply(e0, new Ident("undefined"));
        Type t1 = e1.analyze(env);

        Expr e2 = new Apply(e1, new Ident("undefined"));
        Type t2 = e2.analyze(env);
        
        Type t3 = TypeChecker.makeVariable("t");
        Type t4 = new ConstT("Bool");
        
        TypeChecker.unify(t3, t4);
        
        TypeChecker.unify(t2, t4);
    }

    @Test
    public void testTypeclassCopy() throws HaskellException {
        Env env = new HaskellCatalog().asEnvironment();
        TypeClass num = env.getTypeClasses().get("Num");
        TypeClass read = env.getTypeClasses().get("Show");
        TypeClass show = env.getTypeClasses().get("Read");

        Type ct = new ConstT("Float");
        Type t0 = new TypeVar("a", num, read);
        Type t1 = new TypeVar("b", num, show);

        Expr e0 = new Value(t0, "?");
        Expr e1 = new Value(t1, "?");
        Expr e2 = new Apply(new Apply(new Ident("(+)"), e0), e1);

        e2.analyze(env);

        TypeChecker.unify(t0, ct);
        TypeChecker.unify(t1, ct);
    }
    
    @Test
    public void testInstanceChaining() throws HaskellException {
        Env env = new HaskellCatalog().asEnvironment();

        // (id (id (1 :: Int)))
        Expr e0 = new Apply(new Ident("id"),
            new Apply(new Ident("id"),
                new Value(new ConstT("Int"), "1")));

        Type t0 = e0.analyze(env);

        assertEquals(t0.toHaskellType(), "Int");
    }
}
