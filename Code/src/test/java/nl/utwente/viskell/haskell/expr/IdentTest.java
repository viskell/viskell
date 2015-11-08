package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        assertEquals(Type.fun(this.alpha, this.alpha).prettyPrint(), this.env.useFun("id").inferType().prettyPrint());
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
