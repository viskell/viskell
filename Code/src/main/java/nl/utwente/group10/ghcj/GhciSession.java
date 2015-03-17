package nl.utwente.group10.ghcj;

import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.*;
import nl.utwente.group10.haskell.type.FuncT;

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
     * Uploads a new let binding to ghci.
     */
    public EnvFunc push(UserFunc func) throws HaskellException {
        this.ghci.eval(func.toHaskell());
        return new EnvFunc(func.getName(), (FuncT) func.getType());
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
}
