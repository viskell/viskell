package nl.utwente.group10.ghcj;

import nl.utwente.group10.haskell.expr.*;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GhciSessionTest {
    /** Our session with Ghci. */
    private GhciSession ghci = null;

    private UserFunc pifunc() throws Exception {
        return new UserFunc(
            "my_pi",
            new Value(new ConstT("Float"), "3.14")
        );
    }

    private UserFunc taufunc() throws Exception {
        return new UserFunc(
            "tau",
            new Apply(
                new EnvFunc(
                    "(*)",
                    new FuncT(new ConstT("Float"), new ConstT("Float"), new ConstT("Float"))
                ),
                new Apply(
                    new EnvFunc(
                        "my_pi",
                        new FuncT(new ConstT("Float"))
                    )
                ),
                new Value(new ConstT("Float"), "2")
            )
        );
    }

    @Before
    public void startSession() throws GhciException {
        this.ghci = new GhciSession();
    }

    @After
    public void stopSession() throws Exception {
        this.ghci.close();
    }

    @Test
    public void constFunPush() throws Exception {
        EnvFunc pi = this.ghci.push(this.pifunc());
        Assert.assertEquals("my_pi", pi.toHaskell());
    }

    @Test
    public void constFunPushPull() throws Exception {
        EnvFunc pi = this.ghci.push(this.pifunc());
        Assert.assertEquals("3.14", this.ghci.pull(new Apply(pi)));
    }

    @Test
    public void constFunRef() throws Exception {
        this.ghci.push(this.pifunc());
        EnvFunc tau = this.ghci.push(this.taufunc());
        Assert.assertEquals("6.28", this.ghci.pull(new Apply(tau)));
    }
}
