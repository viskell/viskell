package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.ConstT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CondTest {
    private Env env;
    private GenSet genSet;

    @Before
    public final void setUp() {
        this.env = new Env();
        this.env.addExpr("undefined", "a");

        this.genSet = new GenSet();
    }

    @Test(expected=HaskellTypeError.class)
    public final void testNonBool() throws HaskellException {
        Expr expr = new Cond(
            new Value(new ConstT("Float"), "1.0"),
            new Ident("undefined"),
            new Ident("undefined")
        );

        expr.analyze(env, genSet);
    }

    @Test(expected=HaskellTypeError.class)
    public final void testIncompatible() throws HaskellException {
        Expr expr = new Cond(
            new Value(new ConstT("Bool"), "True"),
            new Value(new ConstT("A"), "foo"),
            new Value(new ConstT("B"), "bar")
        );

        expr.analyze(env, genSet);
    }

    @Test
    public final void testValid() throws HaskellException {
        Expr expr = new Cond(
            new Value(new ConstT("Bool"), "True"),
            new Value(new ConstT("Float"), "1.0"),
            new Value(new ConstT("Float"), "2.0")
        );

        Assert.assertEquals("Float", expr.analyze(env, genSet).prune().toHaskellType());
    }

    @Test
    public final void testInfer() throws HaskellException {
        Expr one = new Cond(
                new Value(new ConstT("Bool"), "True"),
                new Value(HindleyMilner.makeVariable(), "1.0"),
                new Value(new ConstT("Float"), "2.0")
        );

        Expr two = new Cond(
                new Value(new ConstT("Bool"), "True"),
                new Value(new ConstT("Float"), "2.0"),
                new Value(HindleyMilner.makeVariable(), "1.0")
        );

        Assert.assertEquals("Float", one.analyze(env, genSet).prune().toHaskellType());
        Assert.assertEquals("Float", two.analyze(env, genSet).prune().toHaskellType());
    }
}
