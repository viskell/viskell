package nl.utwente.viskell.ghcj;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

public class GhciEvaluatorTest {
    /** Our connection with Ghci. */
    private GhciEvaluator ghci = null;

    /** A newline character. */
    private String NL = null;

    /**
     * Constructs GhciEvaluator, which, at the time of this writing, forks native processes
     * and can fail with exceptions.
     * <p><a href="https://github.com/viskell/viskell/issues/444">see issue #444</a></p>
     * @return an optional GhciEvaluator
     */
    private static Optional<GhciEvaluator> getGhciEvaluator() {
        GhciEvaluator evaluator = null;
        try {
            evaluator = new GhciEvaluator();
        } catch (Exception e) {
            // HaskellException expected when haskell executables are
            // not found installed on the host.
        }
        return Optional.ofNullable(evaluator);
    }

    @Before
    public void startGhci() throws HaskellException {
        Optional<GhciEvaluator> oEvaluator = getGhciEvaluator();
        Assume.assumeTrue("https://github.com/viskell/viskell/issues/444", oEvaluator.isPresent());
        this.ghci = oEvaluator.get();
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
