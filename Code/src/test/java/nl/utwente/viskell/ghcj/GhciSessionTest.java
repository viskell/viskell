package nl.utwente.viskell.ghcj;

import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.Type;
import org.junit.*;

public class GhciSessionTest {
    /** Our session with Ghci. */
    private GhciSession ghci = null;
    private Environment env;

    private Expression pi;

    @Before
    public void setUp() {
        Exception setUpIssue = null;
        try {
            this.env = new Environment();
            this.ghci = new GhciSession();
            this.ghci.startAsync();
            this.ghci.awaitRunning();
            this.env.addTestSignature("my_pi", "Float");
            this.pi = new Value(Type.con("Float"), "3.14");
        } catch (Exception e) {
            setUpIssue = e;
        }
        Assume.assumeNoException(setUpIssue == null ? "" :
            "could not set up GhciSession: "+setUpIssue.getMessage(), setUpIssue);
    }

    @After
    public void tearDown() {
        this.ghci.stopAsync();
        if (ghci.isRunning())
            this.ghci.awaitTerminated();
    }

    @Test
    public void constFunPush() throws HaskellException {
        this.ghci.push("my_pi", this.pi);
        Assert.assertEquals("(3.14)", this.pi.toHaskell());
    }

    @Test
    public void constFunPushPull() throws Exception {
        this.ghci.push("my_pi", this.pi);
        Assert.assertEquals("3.14", this.ghci.pullRaw("my_pi").get());
    }
}
