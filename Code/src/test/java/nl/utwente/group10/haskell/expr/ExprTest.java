package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
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
    private final ConstT integer = new ConstT("Int");
    private final ConstT floating = new ConstT("Float");
    private final ConstT doubl = new ConstT("Double");
    private final ConstT string = new ConstT("String");

    private final TypeClass num = new TypeClass("Num", integer, floating, doubl);
    private final Type numT = new VarT("n", num);

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
                        new ListT(this.integer),
                        "[1, 2, 3, 5, 7]"
                )
        );

        this.env = new Env();
        this.genSet = new GenSet();

        this.env.addTypeClass(this.num);
        this.env.addExpr("(*)", "Num a => a -> a -> a");
        this.env.addExpr("map", "(a -> b) -> [a] -> [b]");
    }

    @Test
    public final void testAnalyze() throws HaskellException {
        assertEquals("[(Int -> Int)]", this.expr.analyze(this.env, this.genSet).prune().toHaskellType());
    }

    @Test
    public final void testGetType() throws HaskellException {
        Type type = this.expr.getType(this.env);

        // Test is object is equal after subsequent call and not equal to the result of analyze
        assertTrue(type == this.expr.getType(env));
        assertFalse(this.expr instanceof Value || type == this.expr.analyze(env)); // Only valid for non-Value exprs.
    }

    @Test(expected = HaskellTypeError.class)
    public final void testTypeclassError() throws HaskellException {
        expr = new Apply(
                new Apply(
                        new Ident("map"),
                        new Ident("(*)")
                ),
                new Value(
                        new ListT(this.string),
                        "[\"a\", \"b\", \"c\"]"
                )
        );
        assertNotEquals("[(String -> String)]", expr.analyze(this.env, this.genSet).prune().toHaskellType());
    }

    @Test
    public final void testToHaskell() {
        assertEquals("((map (*)) [1, 2, 3, 5, 7])", this.expr.toHaskell());
    }
}
