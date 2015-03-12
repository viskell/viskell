package nl.utwente.group10.ghcj;

import java.io.Closeable;
import java.io.IOException;

/**
 * A conversation with an instance of ghci.
 */
public class GhciSession implements Closeable {
    /** The evaluator this GhciSession will communicate with. */
    private final GhciEvaluator ghci;

    /**
     * Builds a new communication session with ghci.
     *
     * @throws GhciException when ghci can not be found, can not be executed,
     *         or does not understand our setup sequence.
     */
    public GhciSession() throws GhciException {
        this.ghci = new GhciEvaluator();
    }

    /**
     * @return a GhciEnvironment that is connected to this GhciSession.
     */
    public final GhciEnvironment getEnvironment() {
        return new GhciEnvironment(this);
    }

    /**
     * @return a String representation of this GhciSession.
     */
    public final String toString() {
        return "GhciSession{" + this.ghci + "}";
    }

    @Override
    public void close() throws IOException {
        this.ghci.close();
    }
}
