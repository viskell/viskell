package nl.utwente.group10.ghcj;

import nl.utwente.group10.haskell.expr.Binding;
import java.util.List;

/**
 * Represents an Environment, that is, a collection of Bindings that lives in
 * a Haskell session, and has methods for querying that collection.
 */
public class GhciEnvironment {
    /** Our parent GhciSession instance. */
    private final GhciSession ghci;

    /**
     * Constructs a fresh GhciEnvironment.
     * @param ghci The parent GhciSession instance.
     */
    public GhciEnvironment(GhciSession ghci) {
        this.ghci = ghci;
    }

    public final List<Binding> getBindings() throws GhciException {
        System.out.println(ghci.eval(":browse"));
        return null;
    }

    @Override
    public final String toString() {
        return "GhciEnvironment{" + "ghci=" + this.ghci + '}';
    }
}
