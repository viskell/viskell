package nl.utwente.viskell.haskell.type;

import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.haskell.expr.*;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class LambdaTest {
    @Test
    public void testLambdaWrapping() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        // wrapping a simple function in a lambda
        Binder x = new Binder("x");
        Binder y = new Binder("y");
        Expression pxy = new Apply (new Apply(env.useFun("(+)"), new LocalVar(x)), new LocalVar(y));
        Expression add = new Lambda(Arrays.asList(x,y), pxy);
        Type tla = add.inferType();
        assertEquals("Num y -> Num y -> Num y", tla.prettyPrint());
        
        // using the same binder twice
        Binder z = new Binder("z");
        Expression ezz = new Apply (new Apply(env.useFun("(^)"), new LocalVar(z)), new LocalVar(z));
        Expression exp = new Lambda(Arrays.asList(z), ezz);
        Type tle = exp.inferType();
        assertEquals("Integral z -> Integral z", tle.prettyPrint());

        Binder u = new Binder("u");
        Expression f5 = new Value(Type.con("Float"), "5.0");
        Expression l5 = new Lambda(Arrays.asList(u), f5);
        assertEquals("u -> Float", l5.inferType().prettyPrint());
    }
    
    @Test
    public void testLambdaAnnotated() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        // wrapping a simple function in a lambda
        Binder x = new Binder("x", Type.con("Int"));
        Binder y = new Binder("y");
        Expression pxy = new Apply (new Apply(env.useFun("(+)"), new LocalVar(x)), new LocalVar(y));
        Expression add = new Lambda(Arrays.asList(x,y), pxy);
        Type tla = add.inferType();
        assertEquals("Int -> Int -> Int", tla.prettyPrint());
        
        // using the same binder twice
        Binder z = new Binder("z", new TypeScope().getVarTC("r", env.testLookupClass("RealFloat")));
        Expression ezz = new Apply (new Apply(env.useFun("(**)"), new LocalVar(z)), new LocalVar(z));
        Expression exp = new Lambda(Arrays.asList(z), ezz);
        Type tle = exp.inferType();
        assertEquals("RealFloat r -> RealFloat r", tle.prettyPrint());

        Binder u = new Binder("u", Type.listOf(Type.con("Int")));
        Expression f5 = new Value(Type.con("Float"), "5.0");
        Expression l5 = new Lambda(Arrays.asList(u), f5);
        assertEquals("[Int] -> Float", l5.inferType().prettyPrint());
    }

    @Test
    public void testResultAnnotation() throws HaskellException {
        Environment env = new HaskellCatalog().asEnvironment();
        
        // wrapping a simple function in a lambda
        Binder x = new Binder("x");
        Binder y = new Binder("y");
        Expression pxy = new Apply (new Apply(env.useFun("(+)"), new LocalVar(x)), new LocalVar(y));
        Expression add = new Lambda(Arrays.asList(x,y), new Annotated(pxy, Type.con("Int")));
        Type tla = add.inferType();
        assertEquals("Int -> Int -> Int", tla.prettyPrint());
        
        // using the same binder twice
        Binder z = new Binder("z");
        Expression ezz = new Apply (new Apply(env.useFun("(^)"), new LocalVar(z)), new LocalVar(z));
        Expression exp = new Lambda(Arrays.asList(z), new Annotated(ezz, Type.con("Integer")));
        Type tle = exp.inferType();
        assertEquals("Integer -> Integer", tle.prettyPrint());

        Binder u = new Binder("u");
        Expression ru = new Apply(env.useFun("read"), new LocalVar(u));
        Expression dr = new Lambda(Arrays.asList(u), new Annotated(ru, Type.con("Double")));
        assertEquals("[Char] -> Double", dr.inferType().prettyPrint());
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
        assertEquals("(Num+Show i) -> [Char]", top.inferType().prettyPrint());

        // testing type of: \s -> (\y -> y `max` y) (read s)
        Binder s = new Binder("s");
        Binder y = new Binder("y");
        Expression myy = new Apply (new Apply(env.useFun("max"), new LocalVar(y)), new LocalVar(y));
        Expression lam = new Lambda(Arrays.asList(y), myy);
        Expression bod = new Apply(lam,  new Apply(env.useFun("read"), new LocalVar(s)));
        Expression res = new Lambda(Arrays.asList(s), bod);
        assertEquals("[Char] -> (Ord+Read a)", res.inferType().prettyPrint());
    }
}