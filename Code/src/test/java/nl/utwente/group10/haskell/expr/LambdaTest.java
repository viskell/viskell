package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.*;

import java.util.Arrays;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.env.HaskellCatalog;
import nl.utwente.group10.haskell.exceptions.CatalogException;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Binder;
import nl.utwente.group10.haskell.expr.Expression;
import nl.utwente.group10.haskell.expr.Lambda;
import nl.utwente.group10.haskell.expr.LocalVar;
import nl.utwente.group10.haskell.expr.Value;
import nl.utwente.group10.haskell.type.Type;

import org.junit.Test;

public class LambdaTest {
    @Test
    public void testLambdaWrapping() throws CatalogException, HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        // wrapping a simple function in a lambda
        Binder x = new Binder("x");
        Binder y = new Binder("y");
        Expression pxy = new Apply (new Apply(env.useFun("(+)"), new LocalVar(x)), new LocalVar(y));
        Expression add = new Lambda(Arrays.asList(x,y), pxy);
        assertEquals("\\ x y -> (((+) x) y)", add.toString());
        
        // using the same binder twice
        Binder z = new Binder("z");
        Expression ezz = new Apply (new Apply(env.useFun("(^)"), new LocalVar(z)), new LocalVar(z));
        Expression exp = new Lambda(Arrays.asList(z), ezz);
        assertEquals("\\ z -> (((^) z) z)", exp.toString());

        Binder u = new Binder("u");
        Expression f5 = new Value(Type.con("Float"), "5.0");
        Expression l5 = new Lambda(Arrays.asList(u), f5);
        assertEquals("\\ u -> 5.0", l5.toString());
    }

}
