package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.type.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExprTest {
    private final TypeCon integer = Type.con("Int");
    private final TypeCon floating = Type.con("Float");
    private final TypeCon doubl = Type.con("Double");
    private final TypeCon string = Type.con("String");

    private final TypeClass num = new TypeClass("Num", integer, floating, doubl);

    private Expression expr;
    private Environment env;

    @Before
    public final void setUp() {
        this.expr = new Apply(
                new Apply(
                        new Ident("map"),
                        new Ident("(*)")
                ),
                new Value(
                        Type.listOf(this.integer),
                        "[1, 2, 3, 5, 7]"
                )
        );

        this.env = new Environment();

        this.env.addTypeClass(this.num);
        this.env.addTestSignature("(*)", "Num a => a -> a -> a");
        this.env.addTestSignature("map", "(a -> b) -> [a] -> [b]");
    }

    @Test
    public final void testAnalyze() throws HaskellException {
        assertEquals("[Int -> Int]", this.expr.findType(this.env).toHaskellType());
    }

    @Test
    public final void testCacheType() throws HaskellException {
        Type type = this.expr.findType(this.env);

        // Test is object is equal after subsequent call
        assertTrue(type == this.expr.findType(env));
    }

    @Test(expected = HaskellTypeError.class)
    public final void testTypeclassError() throws HaskellException {
        expr = new Apply(
                new Apply(
                        new Ident("map"),
                        new Ident("(*)")
                ),
                new Value(
                        Type.listOf(this.string),
                        "[\"a\", \"b\", \"c\"]"
                )
        );
        assertNotEquals("[(String -> String)]", expr.findType(this.env).toHaskellType());
    }

    @Test
    public final void testValueToHaskell() throws HaskellException {
        final Expression v = new Value(this.integer, "10");
        assertEquals(this.integer.toHaskellType(), v.findType(new Environment()).toHaskellType());
        assertEquals("(10)", v.toHaskell());
    }

    @Test
    public final void testToHaskell() {
        assertEquals("((map (*)) ([1, 2, 3, 5, 7]))", this.expr.toHaskell());
    }
}
