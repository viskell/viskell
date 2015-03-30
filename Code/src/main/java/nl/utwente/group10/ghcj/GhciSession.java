package nl.utwente.group10.ghcj;

import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

/**
 * A conversation with an instance of ghci.
 */
public class GhciSession implements Closeable {
    /** The evaluator this GhciSession will communicate with. */
    private final GhciEvaluator ghci;

    /** Singleton instance. */
    private static Optional<GhciSession> instance;

    /**
     * Builds a new communication session with ghci.
     *
     * @throws GhciException when ghci can not be found, can not be executed,
     *         or does not understand our setup sequence.
     */
    private GhciSession() throws GhciException {
        this.ghci = new GhciEvaluator();
    }

    /**
     * Uploads a new let binding to ghci. Returns an Expr instance which should be used instead of the supplied function
     * once the function is pushed.
     * @param name The name of the new function.
     * @param func The actual function.
     * @return The Expr instance to use when accessing the pushed function.
     * @throws HaskellException when the function is rejected by ghci.
     */
    public final Expr push(final String name, final Expr func) throws HaskellException {
        this.ghci.eval(String.format("let %s = %s", name, func.toHaskell()));
        return new Ident(name);
    }

    /**
     * Returns the result of evaluating a Haskell expression.
     */
    public String pull(Expr expr) throws HaskellException {
        return this.ghci.eval(expr.toHaskell()).trim();
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

    public static GhciSession getInstance() throws GhciException {
        if (!instance.isPresent()) {
            instance = Optional.of(new GhciSession());
        }

        return instance.get();
    }
}
