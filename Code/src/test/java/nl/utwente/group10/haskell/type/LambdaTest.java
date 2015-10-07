package nl.utwente.group10.haskell.type;

import static org.junit.Assert.*;

import java.util.Arrays;

import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.env.HaskellCatalog;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Binder;
import nl.utwente.group10.haskell.expr.Expression;
import nl.utwente.group10.haskell.expr.Lambda;
import nl.utwente.group10.haskell.expr.LocalVar;
import nl.utwente.group10.haskell.expr.Value;

import org.junit.Test;

public class LambdaTest {
    @Test
    public void testLambdaWrapping() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        // wrapping a simple function in a lambda
        Binder x = new Binder("x");
        Binder y = new Binder("y");
        Expression pxy = new Apply (new Apply(env.useFun("(+)"), new LocalVar(x)), new LocalVar(y));
        Expression add = new Lambda(Arrays.asList(x,y), pxy);
        Type tla = add.findType();
        assertEquals("Num a -> Num a -> Num a", tla.prettyPrint());
        
        // using the same binder twice
        Binder z = new Binder("z");
        Expression ezz = new Apply (new Apply(env.useFun("(^)"), new LocalVar(z)), new LocalVar(z));
        Expression exp = new Lambda(Arrays.asList(z), ezz);
        Type tle = exp.findType();
        assertEquals("Integral b -> Integral b", tle.prettyPrint());

        Binder u = new Binder("u");
        Expression f5 = new Value(Type.con("Float"), "5.0");
        Expression l5 = new Lambda(Arrays.asList(u), f5);
        assertEquals("u -> Float", l5.findType().prettyPrint());
    }
    
    @Test
    public void testLambdaAnnotated() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        // wrapping a simple function in a lambda
        Binder x = new Binder("x", Type.con("Int"));
        Binder y = new Binder("y");
        Expression pxy = new Apply (new Apply(env.useFun("(+)"), new LocalVar(x)), new LocalVar(y));
        Expression add = new Lambda(Arrays.asList(x,y), pxy);
        Type tla = add.findType();
        assertEquals("Int -> Int -> Int", tla.prettyPrint());
        
        // using the same binder twice
        Binder z = new Binder("z", new TypeScope().getVarTC("r", env.testLookupClass("RealFloat")));
        Expression ezz = new Apply (new Apply(env.useFun("(**)"), new LocalVar(z)), new LocalVar(z));
        Expression exp = new Lambda(Arrays.asList(z), ezz);
        Type tle = exp.findType();
        assertEquals("RealFloat a -> RealFloat a", tle.prettyPrint());

        Binder u = new Binder("u", Type.listOf(Type.con("Int")));
        Expression f5 = new Value(Type.con("Float"), "5.0");
        Expression l5 = new Lambda(Arrays.asList(u), f5);
        assertEquals("[Int] -> Float", l5.findType().prettyPrint());
    }

    @Test
    public void testPropagation() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        // testing type of: \i -> show ((\x -> x + x) i)
        
        Binder i = new Binder("i");
        Binder x = new Binder("x");
        Expression pxx = new Apply (new Apply(env.useFun("(+)"), new LocalVar(x)), new LocalVar(x));
        Expression exp = new Lambda(Arrays.asList(x), pxx);
        Expression app = new Apply(env.useFun("show"), new Apply(exp, new LocalVar(i)));
        Expression top = new Lambda(Arrays.asList(i), app);
        assertEquals("(Num a, Show a) -> [Char]", top.findType().prettyPrint());

        // testing type of: \s -> (\y -> y `max` y) (read s)
        Binder s = new Binder("s");
        Binder y = new Binder("y");
        Expression myy = new Apply (new Apply(env.useFun("max"), new LocalVar(y)), new LocalVar(y));
        Expression lam = new Lambda(Arrays.asList(y), myy);
        Expression bod = new Apply(lam,  new Apply(env.useFun("read"), new LocalVar(s)));
        Expression res = new Lambda(Arrays.asList(s), bod);
        assertEquals("[Char] -> (Ord a, Read a)", res.findType().prettyPrint());
    }
}