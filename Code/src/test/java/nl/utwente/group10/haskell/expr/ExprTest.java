package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.assertEquals;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.ListT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.VarT;

import org.junit.Before;
import org.junit.Test;

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
                        new ListT(new ConstT("Int")),
                        "[1, 2, 3, 5, 7]"
                )
        );

        this.env = new Env();
        this.genSet = new GenSet();

        this.env.put("(*)", new FuncT(this.integer, new FuncT(this.integer, this.integer)));
        this.env.put("map", new FuncT(new FuncT(this.alpha, this.beta), new FuncT(this.alphaList, this.betaList)));
    }

    @Test
    public final void testAnalyze() throws HaskellException {
        assertEquals("[(Int -> Int)]", this.expr.analyze(this.env, this.genSet).prune().toHaskellType());
    }

    @Test
    public final void testToHaskell() {
        assertEquals("((map (*)) [1, 2, 3, 5, 7])", this.expr.toHaskell());
    }
}
