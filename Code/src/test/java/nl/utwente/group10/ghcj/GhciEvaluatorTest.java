package nl.utwente.group10.ghcj;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GhciEvaluatorTest {
    /** Our connection with Ghci. */
    private GhciEvaluator ghci = null;

    /** A newline character. */
    private String NL = null;

    @Before
    public void startGhci() throws HaskellException {
        this.ghci = new GhciEvaluator();
        this.NL = System.getProperty("line.separator");
    }

    @Test
    public void putStrLnTest() throws HaskellException {
        Assert.assertEquals("Hello" + this.NL, this.ghci.eval("putStrLn \"Hello\""));
    }

    @Test
    public void trivialMathTest() throws HaskellException {
        Assert.assertEquals("4" + this.NL, this.ghci.eval("2 + 2"));
    }

    @Test
    public void typeErrorTest() throws HaskellException {
        try {
            this.ghci.eval("map (\\x y -> 10) []");
            Assert.fail("typeErrorTest should throw an exception but didn't");
        } catch (HaskellException e) {
            Assert.assertTrue(e.getMessage().contains("No instance for"));
        }
    }
}
