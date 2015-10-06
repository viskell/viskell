package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.type.Type;

import org.junit.Before;
import org.junit.Test;

public class ApplyTest {
    private final Type beta = Type.var("b");
    private final Type betaList = Type.listOf(this.beta);
    private final Type integer = Type.con("Int");
    private final Type integerList = Type.listOf(this.integer);
    private final Type string = Type.con("String");
    private final Type stringList = Type.listOf(this.string);

    private Environment env;

    @Before
    public final void setUp() {
        this.env = new Environment();

        this.env.addTestSignature("id", "a -> a");
        this.env.addTestSignature("(+)", "Int -> Int -> Int");
        this.env.addTestSignature("map", "(a -> b) -> [a] -> [b]");
        this.env.addTestSignature("zip", "[a] -> [b] -> [(a, b)]");
        this.env.addTestSignature("lcm", "a -> a -> a");
    }

    @Test
    public final void testId() throws HaskellException {
        final Apply apply = new Apply(this.env.useFun("id"), new Value(this.integer, "42"));

        assertEquals("(id (42))", apply.toHaskell());
        assertEquals(this.integer.toHaskellType(), apply.findType().toHaskellType());
    }

    @Test
    public final void testAdd() throws HaskellException {
        final Apply apply1 = new Apply(this.env.useFun("(+)"), new Value(this.integer, "42"));
        final Apply apply2 = new Apply(apply1, new Value(this.integer, "42"));

        assertEquals("((+) (42))", apply1.toHaskell());
        assertEquals("(((+) (42)) (42))", apply2.toHaskell());

        assertEquals(Type.fun(this.integer, this.integer).toHaskellType(), apply1.findType().toHaskellType());
        assertEquals(this.integer.toHaskellType(), apply2.findType().toHaskellType());
    }

    @Test
    public final void testMap() throws HaskellException {
        final Apply apply0 = new Apply(this.env.useFun("(+)"), new Value(this.integer, "42"));
        final Apply apply1 = new Apply(this.env.useFun("map"), apply0);
        final Apply apply2 = new Apply(apply1, new Value(this.integerList, "[1, 2, 3, 5, 7]"));

        assertEquals("(map ((+) (42)))", apply1.toHaskell());
        assertEquals("((map ((+) (42))) ([1, 2, 3, 5, 7]))", apply2.toHaskell());

        assertEquals(Type.fun(this.integerList, this.integerList).toHaskellType(), apply1.findType().toHaskellType());
        assertEquals(this.integerList.toHaskellType(), apply2.findType().toHaskellType());
    }

    @Test
    public final void testZip() throws HaskellException {
        final Apply apply1 = new Apply(this.env.useFun("zip"), new Value(this.integerList, "[1, 2, 3, 5, 7]"));
        final Apply apply2 = new Apply(apply1, new Value(this.stringList, "[\"a\", \"b\", \"c\"]"));

        assertEquals("(zip ([1, 2, 3, 5, 7]))", apply1.toHaskell());
        assertEquals("((zip ([1, 2, 3, 5, 7])) ([\"a\", \"b\", \"c\"]))", apply2.toHaskell());

        assertEquals(Type.fun(this.betaList, Type.listOf(Type.tupleOf(this.integer, this.beta))).toHaskellType(), apply1.findType().toHaskellType());
        assertEquals(Type.listOf(Type.tupleOf(this.integer, this.string)).toHaskellType(), apply2.findType().toHaskellType());
    }

    @Test(expected=HaskellException.class)
    public final void testIncorrectLcm() throws HaskellException {
        final Apply apply1 = new Apply(this.env.useFun("lcm"), new Value(this.integer, "42"));
        final Apply apply2 = new Apply(apply1, new Value(this.string, "\"haskell\""));

        assertEquals("(lcm (42))", apply1.toHaskell());
        assertEquals("((lcm (42)) (\"haskell\"))", apply2.toHaskell());

        assertEquals(Type.fun(this.integer, this.integer).toHaskellType(), apply1.findType().toHaskellType());
        assertNotEquals(this.string, apply2.findType().toHaskellType());
        assertNotEquals(this.integer, apply2.findType().toHaskellType());
    }
}
