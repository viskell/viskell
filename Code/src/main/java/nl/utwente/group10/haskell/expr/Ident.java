package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.Type;

/**
 * Expression contained in the environment.
 */
public class Ident extends Expr {
    /**
     * Name of this identity.
     */
    private final String name;

    public Ident(final String name) {
        this.name = name;
    }

    @Override
    public final Type analyze(final Env env, final GenSet genSet) throws HaskellTypeError {
        // Rule [Var]:
        // IFF  we know (from the env) that the type of this expr is x
        // THEN the type of this expr is x.
        if (env.containsKey(this.name)) {
            return env.get(this.name);
        } else {
            throw new HaskellTypeError(String.format("Expression %s is not known to the environment.", this));
        }
    }

    @Override
    public final String toHaskell() {
        return this.name;
    }

    @Override
    public final String toString() {
        return this.name;
    }
}
