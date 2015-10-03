package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.ListT;
import nl.utwente.group10.haskell.type.TupleT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.VarT;

import org.junit.Before;
import org.junit.Test;

public class ApplyTest {
    private final Type beta = new VarT("b");
    private final Type betaList = new ListT(this.beta);
    private final Type integer = new ConstT("Int");
    private final Type integerList = new ListT(this.integer);
    private final Type string = new ConstT("String");
    private final Type stringList = new ListT(this.string);

    private Env env;

    @Before
    public final void setUp() {
        this.env = new Env();

        this.env.addExpr("id", "a -> a");
        this.env.addExpr("(+)", "Int -> Int -> Int");
        this.env.addExpr("map", "(a -> b) -> [a] -> [b]");
        this.env.addExpr("zip", "[a] -> [b] -> [(a, b)]");
        this.env.addExpr("lcm", "a -> a -> a");
    }

    @Test
    public final void testId() throws HaskellException {
        final Apply apply = new Apply(new Ident("id"), new Value(this.integer, "42"));

        assertEquals("(id 42)", apply.toHaskell());
        assertEquals(this.integer.toHaskellType(), apply.analyze(this.env).prune().toHaskellType());
    }

    @Test
    public final void testAdd() throws HaskellException {
        final Apply apply1 = new Apply(new Ident("(+)"), new Value(this.integer, "42"));
        final Apply apply2 = new Apply(apply1, new Value(this.integer, "42"));

        assertEquals("((+) 42)", apply1.toHaskell());
        assertEquals("(((+) 42) 42)", apply2.toHaskell());

        assertEquals(new FuncT(this.integer, this.integer).toHaskellType(), apply1.analyze(this.env).prune().toHaskellType());
        assertEquals(this.integer.toHaskellType(), apply2.analyze(this.env).prune().toHaskellType());
    }

    @Test
    public final void testMap() throws HaskellException {
        final Apply apply0 = new Apply(new Ident("(+)"), new Value(this.integer, "42"));
        final Apply apply1 = new Apply(new Ident("map"), apply0);
        final Apply apply2 = new Apply(apply1, new Value(this.integerList, "[1, 2, 3, 5, 7]"));

        assertEquals("(map ((+) 42))", apply1.toHaskell());
        assertEquals("((map ((+) 42)) [1, 2, 3, 5, 7])", apply2.toHaskell());

        assertEquals(new FuncT(this.integerList, this.integerList).toHaskellType(), apply1.analyze(this.env).prune().toHaskellType());
        assertEquals(this.integerList.toHaskellType(), apply2.analyze(this.env).prune().toHaskellType());
    }

    @Test
    public final void testZip() throws HaskellException {
        final Apply apply1 = new Apply(new Ident("zip"), new Value(this.integerList, "[1, 2, 3, 5, 7]"));
        final Apply apply2 = new Apply(apply1, new Value(this.stringList, "[\"a\", \"b\", \"c\"]"));

        assertEquals("(zip [1, 2, 3, 5, 7])", apply1.toHaskell());
        assertEquals("((zip [1, 2, 3, 5, 7]) [\"a\", \"b\", \"c\"])", apply2.toHaskell());

        assertEquals(new FuncT(this.betaList, new ListT(new TupleT(this.integer, this.beta))).toHaskellType(), apply1.analyze(this.env).prune().toHaskellType());
        assertEquals(new ListT(new TupleT(this.integer, this.string)).toHaskellType(), apply2.analyze(this.env).prune().toHaskellType());
    }

    @Test(expected=HaskellException.class)
    public final void testIncorrectLcm() throws HaskellException {
        final Apply apply1 = new Apply(new Ident("lcm"), new Value(this.integer, "42"));
        final Apply apply2 = new Apply(apply1, new Value(this.string, "\"haskell\""));

        assertEquals("(lcm 42)", apply1.toHaskell());
        assertEquals("((lcm 42) \"haskell\")", apply2.toHaskell());

        assertEquals(new FuncT(this.integer, this.integer).toHaskellType(), apply1.analyze(this.env).prune().toHaskellType());
        assertNotEquals(this.string, apply2.analyze(this.env).prune().toHaskellType());
        assertNotEquals(this.integer, apply2.analyze(this.env).prune().toHaskellType());
    }
}
