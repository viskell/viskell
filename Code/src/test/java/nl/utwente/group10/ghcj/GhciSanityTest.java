package nl.utwente.group10.ghcj;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GhciSanityTest {
    private GhciSession ghci;

    @Before
    public void startGhci() throws GhciException {
        this.ghci = new GhciSession();
    }

    @After
    public void stopGhci() throws Exception {
        this.ghci.close();
    }

    @Test
    public void putStrLnTest() throws GhciException {
        Assert.assertEquals("Hello\n", this.ghci.eval("putStrLn \"Hello\""));
    }

    @Test
    public void trivialMathTest() throws GhciException {
        Assert.assertEquals("4\n", this.ghci.eval("2 + 2"));
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
