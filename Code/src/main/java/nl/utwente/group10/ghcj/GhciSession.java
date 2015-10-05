package nl.utwente.group10.ghcj;

import java.io.Closeable;
import java.io.IOException;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellSyntaxError;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.type.Type;

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
     * Uploads a new let binding to ghci
     * @param name The name of the new function.
     * @param func The actual function.
     * @throws HaskellException when the function is rejected by ghci.
     */
    public void push(final String name, final Expr func) throws HaskellException {
        try {
            this.ghci.eval(String.format("let %s = %s", name, func.toHaskell()));
        } catch (HaskellSyntaxError e) {
            throw new HaskellSyntaxError(e.getMessage(), func);
        } catch (HaskellException e) {
            throw new HaskellException(e.getMessage(), func);
        }
    }

    /**
     * Returns the result of evaluating a Haskell expression.
     * @param expr The expression to evaluate.
     * @return The result of the evaluation.
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
     * Returns the result of evaluating something in ghci.
     * Should only be used for testing purposes or for a known valid Haskell expression. 
     * @param expr The string representation of the expression to evaluate.
     * @return The result of the evaluation.
     * @throws HaskellException when ghci encountered an error.
     */
    public String pullRaw(final String expr) throws HaskellException {
        return this.ghci.eval(expr).trim();
    }
    
    /**
     * Ask ghci for the type of an expression
     * @param expr The expression String to determine the type of.
     * @param env The environment in which the type will be resolved 
     * @return The parsed Haskell type
     * @throws HaskellException when  ghci encountered an error or the type could not be parsed.
     */
    public Type pullType(final String expr, Env env) throws HaskellException {
        String[] parts = this.pullRaw(":t (" + expr + ")").split(" :: ");
        if (parts.length < 2) {
            throw new HaskellException("ghci could not determine the type of:/n" + expr);
        }
            
        return env.buildType(parts[1].trim());
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
