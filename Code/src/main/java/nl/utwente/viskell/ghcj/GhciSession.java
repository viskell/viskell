package nl.utwente.viskell.ghcj;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import nl.utwente.viskell.haskell.env.Environment;
import nl.utwente.viskell.haskell.env.HaskellCatalog;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.Main;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

/**
 * A conversation with an instance of ghci.
 *
 * Public methods are safe to use from multiple threads.
 */
public final class GhciSession extends AbstractExecutionThreadService {
    /** Work queue. */
    private ArrayBlockingQueue<AbstractMap.SimpleEntry<String, SettableFuture<String>>> queue;

    /** Stuff this into the work queue to stop running. */
    private final static String POISON = null;

    /** The evaluator this GhciSession will communicate with. */
    private Evaluator ghci;

    public enum Backend {
        GHCi,
        Clash,
    }

    /**
     * Builds a new communication session with ghci.
     *
     * Starting the backend is delayed until startAsync() is called.
     */
    public GhciSession() {
        super();

        queue = new ArrayBlockingQueue<>(1024);
    }

    @Override
    protected void run() throws Exception {
        while (true) {
            AbstractMap.SimpleEntry<String, SettableFuture<String>> x = queue.take();

            String expr = x.getKey();
            SettableFuture<String> future = x.getValue();

            if (Objects.equals(expr, POISON)) {
                // Something wants us to quit - do so.
                break;
            } else {
                try {
                    String result = this.ghci.eval(expr);
                    future.set(result.trim());
                } catch (HaskellException e) {
                    future.setException(e);
                }
            }
        }
    }

    /**
     * Uploads a new let binding to ghci
     * @param name The name of the new function.
     * @param func The actual function.
     */
    public ListenableFuture<String> push(final String name, final Expression func) {
        String let = String.format("let %s = %s", name, func.toHaskell());
        return pullRaw(let);
    }

    /**
     * Returns the result of evaluating a Haskell expression.
     * @param expr The expression to evaluate.
     * @return The result of the evaluation.
     */
    public ListenableFuture<String> pull(final Expression expr) {
        return pullRaw(expr.toHaskell());
    }

    /**
     * Returns the result of evaluating something in ghci.
     * Should only be used for testing purposes or for a known valid Haskell expression. 
     * @param expr The string representation of the expression to evaluate.
     * @return The result of the evaluation.
     */
    public ListenableFuture<String> pullRaw(final String expr) {
        SettableFuture<String> result = SettableFuture.create();
        AbstractMap.SimpleEntry<String, SettableFuture<String>> entry = new AbstractMap.SimpleEntry<>(expr, result);

        try {
            queue.put(entry);
        } catch (InterruptedException e) {
            result.setException(e);
        }

        return result;
    }
    
    /**
     * Ask ghci for the type of an expression
     * @param expr The expression String to determine the type of.
     * @param env The environment in which the type will be resolved 
     * @return The parsed Haskell type
     * @throws HaskellException when  ghci encountered an error or the type could not be parsed.
     */
    public Type pullType(final String expr, Environment env) throws HaskellException {
        try {
            String[] parts = this.pullRaw(":t " + expr).get().split(" :: ");

            if (parts.length < 2) {
                throw new HaskellException("ghci could not determine the type of:\n" + expr);
            }

            return env.buildType(parts[1].trim());
        } catch (InterruptedException | ExecutionException e) {
            throw new HaskellException(e);
        }
    }

    @Override
    protected void triggerShutdown() {
        queue.offer(new AbstractMap.SimpleEntry<>(POISON, null));
    }

    /**
     * @return a String representation of this GhciSession.
     */
    public String toString() {
        return "GhciSession{" + this.ghci + "}";
    }

    @Override
    public void shutDown() throws IOException {
        try {
            this.ghci.close();
            this.ghci = null;
        } catch (HaskellException e) {
            e.printStackTrace();
        }
    }

    /** Build a new Evaluator, closing the old one if it exists. */
    @Override
    public void startUp() throws HaskellException {
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

    public HaskellCatalog getCatalog() {
        awaitRunning();
        return new HaskellCatalog(this.ghci.getCatalogPath());
    }
}
