package nl.utwente.group10.ghcj;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GhciSanityTest {
    /** Our connection with Ghci. */
    private GhciSession ghci;

    /** A newline character. */
    private final String NL;

    @Before
    public void startGhci() throws GhciException {
        this.ghci = new GhciSession();
        this.NL = System.getProperty("line.separator");
    }

    @After
    public void stopGhci() throws Exception {
        this.ghci.close();
    }

    @Test
    public void putStrLnTest() throws GhciException {
        Assert.assertEquals("Hello" + NL, this.ghci.eval("putStrLn \"Hello\""));
    }

    @Test
    public void trivialMathTest() throws GhciException {
        Assert.assertEquals("4" + NL, this.ghci.eval("2 + 2"));
    }

    @Test
    public void typeErrorTest() throws GhciException {
        try {
            this.ghci.eval("map (\\x y -> 10) []");
            Assert.fail("typeErrorTest should throw an exception but didn't");
        } catch (HaskellException e) {
            Assert.assertTrue(e.getMessage().contains("No instance for"));
        }
    }
}
