package nl.utwente.viskell.ghcj;

import com.google.common.collect.Lists;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.Main;

import java.io.Closeable;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * A conversation with an instance of ghci.
 */
public final class GhciSession implements Closeable {
    /** The evaluator this GhciSession will communicate with. */
    private Evaluator ghci;

    public enum Backend {
        GHCi,
        Clash,
    }

    /**
     * Builds a new communication session with ghci.
     *
     * @throws HaskellException when ghci can not be found, can not be executed,
     *         or does not understand our setup sequence.
     */
    public GhciSession() throws HaskellException {
        start();
    }

    /**
     * Uploads a new let binding to ghci
     * @param name The name of the new function.
     * @param func The actual function.
     * @throws HaskellException when the function is rejected by ghci.
     */
    public void push(final String name, final Expression func) throws HaskellException {
        try {
            this.ghci.eval(String.format("let %s = %s", name, func.toHaskell()));
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
    public String pull(final Expression expr) throws HaskellException {
        try {
            return this.ghci.eval(expr.toHaskell()).trim();
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
    public Type pullType(final String expr, Environment env) throws HaskellException {
        String[] parts = this.pullRaw(":t " + expr).split(" :: ");
        if (parts.length < 2) {
            throw new HaskellException("ghci could not determine the type of:\n" + expr);
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
        try {
            this.ghci.close();
            this.ghci = null;
        } catch (HaskellException e) {
            e.printStackTrace();
        }
    }

    /** Build a new Evaluator, closing the old one if it exists. */
    public void start() throws HaskellException {
        if (this.ghci != null) {
            this.ghci.close();
        }

        this.ghci = evaluatorFactory(pickBackend());
    }

    /** Build the Evaluator that corresponds to the given Backend identifier. */
    private Evaluator evaluatorFactory(Backend evaluator) throws HaskellException {
        switch (evaluator) {
            case GHCi:  return new GhciEvaluator();
            case Clash: return new ClashEvaluator();
            default:    return new GhciEvaluator();
        }
    }

    /** @return the Backend in the preferences, or GHCi otherwise. */
    public static Backend pickBackend() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String name = prefs.get("ghci", Backend.GHCi.name());
        return Backend.valueOf(name);
    }

    /** @return the available backend identifiers. */
    public static List<Backend> getBackends() {
        return Lists.newArrayList(EnumSet.allOf(Backend.class));
    }
}
