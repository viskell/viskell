package nl.utwente.group10.ghcj;

import org.junit.Assert;
import org.junit.Test;

public class GhciSanityTest {
    private final GhciSession ghci;

    public GhciSanityTest() throws GhciException {
        this.ghci = new GhciSession();
    }

    @Test
    public void putStrLnTest() throws GhciException {
        Assert.assertEquals("Hello\n", ghci.eval("putStrLn \"Hello\""));
    }

    @Test
    public void trivialMathTest() throws GhciException {
        Assert.assertEquals("4\n", ghci.eval("2 + 2"));
    }
}
