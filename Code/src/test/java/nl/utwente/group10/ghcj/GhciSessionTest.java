package nl.utwente.group10.ghcj;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.expr.Expression;
import nl.utwente.group10.haskell.expr.Value;
import nl.utwente.group10.haskell.type.Type;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GhciSessionTest {
    /** Our session with Ghci. */
    private GhciSession ghci = null;
    private Environment env;

    private Expression pi;

    @Before
    public void setUp() throws HaskellException {
        this.env = new Environment();
        this.ghci = new GhciSession();

        this.env.addTestSignature("my_pi", "Float");
        this.pi = new Value(Type.con("Float"), "3.14");
    }

    @Test
    public void constFunPush() throws HaskellException {
        this.ghci.push("my_pi", this.pi);
        Assert.assertEquals("(3.14)", this.pi.toHaskell());
    }

    @Test
    public void constFunPushPull() throws HaskellException {
        this.ghci.push("my_pi", this.pi);
        Assert.assertEquals("3.14", this.ghci.pullRaw("my_pi"));
    }
}
