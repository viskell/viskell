package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExprTest {
    private final Type alpha = new VarT("a");
    private final Type beta = new VarT("b");
    private final Type alphaList = new ListT(this.alpha);
    private final Type betaList = new ListT(this.beta);
    private final Type integer = new ConstT("Int");

    private Expr expr;
    private Env env;
    private GenSet genSet;

    @Before
    public final void setUp() {
        this.expr = new Apply(
                new Apply(
                        new Ident("map"),
                        new Ident("(*)")
                ),
                new Value(
                        new ListT(new ConstT("Integer")),
                        "[1, 2, 3, 5, 7]"
                )
        );

        this.env = new Env();
        this.genSet = new GenSet();

        this.env.put("(*)", new FuncT(this.integer, new FuncT(this.integer, this.integer)));
        this.env.put("map", new FuncT(new FuncT(this.alpha, this.beta), new FuncT(this.alphaList, this.betaList)));
    }

    @Test
    public final void testAnalyze() throws HaskellTypeError {
        assertEquals("[Integer]", this.expr.analyze(this.env, this.genSet).prune().toHaskellType());
    }

    @Test
    public final void testToHaskell() {
        assertEquals("((map (*)) [1, 2, 3, 5, 7])", this.expr.toHaskell());
    }
}