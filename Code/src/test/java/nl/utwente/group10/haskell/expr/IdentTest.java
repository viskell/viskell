package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.assertEquals;

import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeScope;

import org.junit.Before;
import org.junit.Test;

public class IdentTest {
    private Type alpha;
    private Environment env;

    @Before
    public final void setUp() {
        this.env = new Environment();
        TypeScope scope = new TypeScope();
        this.alpha = scope.getVar("a");
        this.env.addTestSignature("id", "a -> a");
    }

    @Test
    public final void testAnalyze() throws HaskellException {
        assertEquals(Type.fun(this.alpha, this.alpha).prettyPrint(), this.env.useFun("id").findType().prettyPrint());
    }

    @Test
    public final void testToHaskell() throws HaskellException {
        assertEquals("id", this.env.useFun("id").toHaskell());
    }

    @Test(expected=HaskellException.class)
    public final void testIncorrectName() throws HaskellException {
        new Environment().useFun("id");
    }
}
