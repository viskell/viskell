package nl.utwente.group10.ghcj;

import java.io.Closeable;
import java.io.IOException;

import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellSyntaxError;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;

/**
 * A conversation with an instance of ghci.
 */
public final class GhciSession implements Closeable {
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
     * Uploads a new let binding to ghci. Returns an Expr instance which should be used instead of the supplied function
     * once the function is pushed.
     * @param name The name of the new function.
     * @param func The actual function.
     * @return The Expr instance to use when accessing the pushed function.
     * @throws HaskellException when the function is rejected by ghci.
     */
    public Expr push(final String name, final Expr func) throws HaskellException {
        this.ghci.eval(String.format("let %s = %s", name, func.toHaskell()));
        return new Ident(name);
    }

    /**
     * Returns the result of evaluating a Haskell expression.
     * @param expr The expression to evaluate.
     * @return The result of the expression.
     * @throws HaskellException when ghci encountered an error.
     */
    public String pull(final Expr expr) throws HaskellException {
        try {
            return this.ghci.eval(expr.toHaskell()).trim();
        } catch (HaskellSyntaxError e) {
            throw new HaskellSyntaxError(e.getMessage(), expr);
        } catch (HaskellException e) {
            throw new HaskellException(e.getMessage(), expr);
        }
    }

    /**
     * @return a String representation of this GhciSession.
     */
    public String toString() {
        return "GhciSession{" + this.ghci + "}";
    }

    @Override
    public void close() throws IOException {
        this.ghci.close();
    }
}
