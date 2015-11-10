package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ApplyTest {
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
        assertEquals(this.integer.prettyPrint(), apply.inferType().prettyPrint());
    }

    @Test
    public final void testAdd() throws HaskellException {
        final Apply apply1 = new Apply(this.env.useFun("(+)"), new Value(this.integer, "42"));
        final Apply apply2 = new Apply(apply1, new Value(this.integer, "42"));

        assertEquals("((+) (42))", apply1.toHaskell());
        assertEquals("(((+) (42)) (42))", apply2.toHaskell());

        assertEquals(Type.fun(this.integer, this.integer).prettyPrint(), apply1.inferType().prettyPrint());
        assertEquals(this.integer.prettyPrint(), apply2.inferType().prettyPrint());
    }

    @Test
    public final void testMap() throws HaskellException {
        final Apply apply0 = new Apply(this.env.useFun("(+)"), new Value(this.integer, "42"));
        final Apply apply1 = new Apply(this.env.useFun("map"), apply0);
        final Apply apply2 = new Apply(apply1, new Value(this.integerList, "[1, 2, 3, 5, 7]"));

        assertEquals("(map ((+) (42)))", apply1.toHaskell());
        assertEquals("((map ((+) (42))) ([1, 2, 3, 5, 7]))", apply2.toHaskell());

        assertEquals(Type.fun(this.integerList, this.integerList).prettyPrint(), apply1.inferType().prettyPrint());
        assertEquals(this.integerList.prettyPrint(), apply2.inferType().prettyPrint());
    }

    @Test
    public final void testZip() throws HaskellException {
        TypeScope scope = new TypeScope();
        final Type beta = scope.getVar("b");
        final Type betaList = Type.listOf(beta);
        
        final Apply apply1 = new Apply(this.env.useFun("zip"), new Value(this.integerList, "[1, 2, 3, 5, 7]"));
        final Apply apply2 = new Apply(apply1, new Value(this.stringList, "[\"a\", \"b\", \"c\"]"));

        assertEquals("(zip ([1, 2, 3, 5, 7]))", apply1.toHaskell());
        assertEquals("((zip ([1, 2, 3, 5, 7])) ([\"a\", \"b\", \"c\"]))", apply2.toHaskell());

        assertEquals(Type.fun(betaList, Type.listOf(Type.tupleOf(this.integer, beta))).prettyPrint(), apply1.inferType().prettyPrint());
        assertEquals(Type.listOf(Type.tupleOf(this.integer, this.string)).prettyPrint(), apply2.inferType().prettyPrint());
    }

    @Test(expected=HaskellException.class)
    public final void testIncorrectLcm() throws HaskellException {
        final Apply apply1 = new Apply(this.env.useFun("lcm"), new Value(this.integer, "42"));
        final Apply apply2 = new Apply(apply1, new Value(this.string, "\"haskell\""));

        assertEquals("(lcm (42))", apply1.toHaskell());
        assertEquals("((lcm (42)) (\"haskell\"))", apply2.toHaskell());

        assertEquals(Type.fun(this.integer, this.integer).prettyPrint(), apply1.inferType().prettyPrint());
        assertNotEquals(this.string, apply2.inferType().prettyPrint());
        assertNotEquals(this.integer, apply2.inferType().prettyPrint());
    }
}
