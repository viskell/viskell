package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.Type;

import java.util.Optional;

/**
 * Expression contained in the environment.
 */
public class Ident extends Expr {
    /**
     * Name of this identifier.
     */
    private final String name;

    /**
     * Constructs a new identifier to use with functions that are known to the Haskell environment.
     * @param name The identifier. Be sure that this exists in the environment as it would cause exceptions elsewhere if
     *             this is not the case.
     */
    public Ident(final String name) {
        this.name = name;
    }

    @Override
    public final Type analyze(final Env env) throws HaskellException {
        // Rule [Var]:
        // IFF  we know (from the env) that the type of this expr is x
        // THEN the type of this expr is x.
        Optional<Type> type = env.getFreshExprType(this.name);
        if (type.isPresent()) {
            this.setCachedType(type.get());
            return type.get();
        } else {
            this.setCachedType(null);
            Expr.logger.warning(String.format("Expression %s is not in the environment, but it is assumed to be.", this));
            throw new HaskellException("Expression not in environment", this);
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
