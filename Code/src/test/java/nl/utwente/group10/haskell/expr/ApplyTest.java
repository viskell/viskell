package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApplyTest {
    private final Type alpha = new VarT("a");
    private final Type beta = new VarT("b");
    private final Type gamma = new VarT("c");
    private final Type alphaList = new ListT(this.alpha);
    private final Type betaList = new ListT(this.beta);
    private final Type integer = new ConstT("Int");
    private final Type integerList = new ListT(this.integer);
    private final Type string = new ConstT("Str");
    private final Type stringList = new ListT(this.string);

    private Env env;
    private GenSet genSet;

    @Before
    public final void setUp() {
        this.env = new Env();
        this.genSet = new GenSet();

        this.env.put("id", new FuncT(this.gamma, this.gamma));
        this.env.put("(+)", new FuncT(this.integer, new FuncT(this.integer, this.integer)));
        this.env.put("map", new FuncT(new FuncT(this.alpha, this.beta), new FuncT(this.alphaList, this.betaList)));
        this.env.put("zip", new FuncT(this.alphaList, new FuncT(this.betaList, new ListT(new TupleT(this.alpha, this.beta)))));
        this.env.put("lcm", new FuncT(this.alpha, new FuncT(this.alpha, this.alpha)));
    }

    @Test
    public final void testId() throws HaskellTypeError {
        final Apply apply = new Apply(new Ident("id"), new Value(this.integer, "42"));

        assertEquals("(id 42)", apply.toHaskell());
        assertEquals(this.integer.toHaskellType(), apply.analyze(this.env, this.genSet).prune().toHaskellType());
    }

    @Test
    public final void testAdd() throws HaskellTypeError {
        final Apply apply1 = new Apply(new Ident("(+)"), new Value(this.integer, "42"));
        final Apply apply2 = new Apply(apply1, new Value(this.integer, "42"));

        assertEquals("((+) 42)", apply1.toHaskell());
        assertEquals("(((+) 42) 42)", apply2.toHaskell());

        assertEquals(new FuncT(this.integer, this.integer).toHaskellType(), apply1.analyze(this.env, this.genSet).prune().toHaskellType());
        assertEquals(this.integer.toHaskellType(), apply2.analyze(this.env, this.genSet).prune().toHaskellType());
    }

    @Test
    public final void testMap() throws HaskellTypeError {
        final Apply apply0 = new Apply(new Ident("(+)"), new Value(this.integer, "42"));
        final Apply apply1 = new Apply(new Ident("map"), apply0);
        final Apply apply2 = new Apply(apply1, new Value(this.integerList, "[1, 2, 3, 5, 7]"));

        assertEquals("(map ((+) 42))", apply1.toHaskell());
        assertEquals("((map ((+) 42)) [1, 2, 3, 5, 7])", apply2.toHaskell());

        assertEquals(new FuncT(this.integerList, this.integerList).toHaskellType(), apply1.analyze(this.env, this.genSet).prune().toHaskellType());
        assertEquals(this.integerList.toHaskellType(), apply2.analyze(this.env, this.genSet).prune().toHaskellType());
    }

    @Test
    public final void testZip() throws HaskellTypeError {
        final Apply apply1 = new Apply(new Ident("zip"), new Value(this.integerList, "[1, 2, 3, 5, 7]"));
        final Apply apply2 = new Apply(apply1, new Value(this.stringList, "[\"a\", \"b\", \"c\"]"));

        assertEquals("(zip [1, 2, 3, 5, 7])", apply1.toHaskell());
        assertEquals("((zip [1, 2, 3, 5, 7]) [\"a\", \"b\", \"c\"])", apply2.toHaskell());

        assertEquals(new FuncT(this.betaList, new ListT(new TupleT(this.integer, this.beta))).toHaskellType(), apply1.analyze(this.env, this.genSet).prune().toHaskellType());
        assertEquals(new ListT(new TupleT(this.integer, this.string)).toHaskellType(), apply2.analyze(this.env, this.genSet).prune().toHaskellType());
    }

    @Test(expected=HaskellTypeError.class)
    public final void testIncorrectLcm() throws HaskellTypeError {
        final Apply apply1 = new Apply(new Ident("lcm"), new Value(this.integer, "42"));
        final Apply apply2 = new Apply(apply1, new Value(this.string, "\"haskell\""));

        assertEquals("(lcm 42)", apply1.toHaskell());
        assertEquals("((lcm 42) \"haskell\")", apply2.toHaskell());

        assertEquals(new FuncT(this.integer, this.integer).toHaskellType(), apply1.analyze(this.env, this.genSet).prune().toHaskellType());
        assertNotEquals(this.string, apply2.analyze(this.env, this.genSet).prune().toHaskellType());
        assertNotEquals(this.integer, apply2.analyze(this.env, this.genSet).prune().toHaskellType());
    }
}
