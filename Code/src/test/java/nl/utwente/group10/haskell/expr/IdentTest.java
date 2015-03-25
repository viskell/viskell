package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.VarT;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IdentTest {
    private final Type alpha = new VarT("a");
    private final Type integer = new ConstT("Integer");

    private Env env;
    private GenSet genSet;

    @Before
    public final void setUp() {
        this.env = new Env();
        this.genSet = new GenSet();

        this.env.put("id", new FuncT(this.alpha, this.alpha));
    }

    @Test
    public final void testAnalyze() throws HaskellTypeError {
        assertEquals(new FuncT(this.alpha, this.alpha), new Ident("id").analyze(this.env, this.genSet));
    }

    @Test
    public final void testToHaskell() throws HaskellTypeError {
        assertEquals("id", new Ident("id").toHaskell());
    }

    @Test(expected=HaskellTypeError.class)
    public final void testIncorrectName() throws HaskellTypeError {
        new Ident("id").analyze(new Env(), new GenSet());
        assertTrue("Invalid state reached: exception not raised.", false);
    }
}