package nl.utwente.group10.ghcj;

import org.junit.Assert;
import org.junit.Test;

public class GhciSanityTest {
    /** Our connection with Ghci. */
    private final GhciSession ghci;

    /** A newline character. */
    private final String NL;

    public GhciSanityTest() throws GhciException {
        this.ghci = new GhciSession();
        this.NL = System.getProperty("line.separator");
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
