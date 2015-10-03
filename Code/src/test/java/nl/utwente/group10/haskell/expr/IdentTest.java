package nl.utwente.group10.haskell.expr;

import static org.junit.Assert.assertEquals;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.VarT;

import org.junit.Before;
import org.junit.Test;

public class IdentTest {
    private final Type alpha = new VarT("a");

    private Env env;

    @Before
    public final void setUp() {
        this.env = new Env();

        this.env.addExpr("id", "a -> a");
    }

    @Test
    public final void testAnalyze() throws HaskellException {
        assertEquals(new FuncT(this.alpha, this.alpha).toHaskellType(), new Ident("id").analyze(this.env).toHaskellType());
    }

    @Test
    public final void testToHaskell() throws HaskellException {
        assertEquals("id", new Ident("id").toHaskell());
    }

    @Test(expected=HaskellException.class)
    public final void testIncorrectName() throws HaskellException {
        new Ident("id").analyze(new Env());
    }
}
