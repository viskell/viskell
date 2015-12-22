package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.haskell.type.Type;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

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
        assertEquals("(\\ x y -> (((+) x) y))", add.toString());
        
        // using the same binder twice
        Binder z = new Binder("z");
        Expression ezz = new Apply (new Apply(env.useFun("(^)"), new LocalVar(z)), new LocalVar(z));
        Expression exp = new Lambda(Arrays.asList(z), ezz);
        assertEquals("(\\ z -> (((^) z) z))", exp.toString());

        Binder u = new Binder("u");
        Expression f5 = new Value(Type.con("Float"), "5.0");
        Expression l5 = new Lambda(Arrays.asList(u), f5);
        assertEquals("(\\ u -> 5.0)", l5.toString());
        
        // using a zero-argument lambda
        Expression emptyLambda = new Lambda(Collections.emptyList(), new Value(Type.con("Float"), "pi"));
        assertEquals("(\\_ -> pi) ()", emptyLambda.toString());
    }

}
