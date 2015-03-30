package nl.utwente.group10.ghcj;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.*;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GhciSessionTest {
    /** Our session with Ghci. */
    private GhciSession ghci = null;
    private Env env;
    private GenSet genSet;

    private Expr pi;

    @Before
    public void setUp() throws HaskellException {
        this.env = new Env();
        this.genSet = new GenSet();

        this.env.put("my_pi", new ConstT("Float"));
        this.pi = new Value(new ConstT("Float"), "3.14");
    }

    @Before
    public void startSession() throws GhciException {
        this.ghci = GhciSession.getInstance();
    }

    @After
    public void stopSession() throws Exception {
        this.ghci.close();
    }

    @Test
    public void constFunPush() throws Exception {
        this.ghci.push("my_pi", this.pi);
        Assert.assertEquals("3.14", this.pi.toHaskell());
    }

    @Test
    public void constFunPushPull() throws Exception {
        this.ghci.push("my_pi", this.pi);
        Assert.assertEquals("3.14", this.ghci.pull(new Ident("my_pi")));
    }
}
