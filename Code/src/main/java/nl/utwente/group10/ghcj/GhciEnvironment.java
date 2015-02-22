package nl.utwente.group10.ghcj;

import com.google.common.base.Splitter;
import nl.utwente.group10.haskell.expr.Binding;

import java.util.ArrayList;
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
    public GhciEnvironment(final GhciSession ghci) {
        this.ghci = ghci;
    }

    /**
     * Query the parent GHCI instance for a list of known functions.
     * @return all functions known to GHCI as Binding instances.
     * @throws GhciException when GHCI is not ready for our query.
     */
    public final List<Binding> getBindings() throws GhciException {
        String input = this.ghci.eval(":browse");
        List<Binding> out = new ArrayList<Binding>();

        for (String line : Splitter.on('\n').split(input)) {
            out.add(this.parseBinding(line));
        }

        return out;
    }

    /**
     * Parses a Haskell type into a Binding.
     * @param line A Haskell type declaration.
     * @throws GhciException when the line is not valid.
     * @return A Binding object matching the declaration.
     */
    private Binding parseBinding(final String line) throws GhciException {
        return null;
    }

    @Override
    public final String toString() {
        return "GhciEnvironment{" + "ghci=" + this.ghci + '}';
    }
}
