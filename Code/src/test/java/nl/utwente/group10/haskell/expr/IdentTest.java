package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.assertEquals;
import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.Type;

import org.junit.Before;
import org.junit.Test;

public class IdentTest {
    private final Type alpha = Type.var("a");

    private Environment env;

    @Before
    public final void setUp() {
        this.env = new Environment();

        this.env.addExpr("id", "a -> a");
    }

    @Test
    public final void testAnalyze() throws HaskellException {
        assertEquals(Type.fun(this.alpha, this.alpha).toHaskellType(), new Ident("id").findType(this.env).toHaskellType());
    }

    @Test
    public final void testToHaskell() throws HaskellException {
        assertEquals("id", new Ident("id").toHaskell());
    }

    @Test(expected=HaskellException.class)
    public final void testIncorrectName() throws HaskellException {
        new Ident("id").findType(new Environment());
    }
}
