package nl.utwente.group10.ghcj;

import org.junit.Test;
import static org.junit.Assert.*;

public class GhciSanityTest {
    private GhciSession ghci;

    public GhciSanityTest() throws Exception {
        ghci = new GhciSession();
    }

    @Test
    public void putStrLnTest() throws GhciException {
        assertEquals("Hello\n", ghci.eval("putStrLn \"Hello\""));
    }

    @Test
    public void trivialMathTest() throws GhciException {
        assertEquals("4\n", ghci.eval("2 + 2"));
    }
}
