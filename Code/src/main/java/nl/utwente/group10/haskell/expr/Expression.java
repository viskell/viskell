package nl.utwente.group10.haskell.expr;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import nl.utwente.group10.haskell.HaskellObject;
import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.Type;

/**
 * An expression in Haskell.
 */
public abstract class Expression extends HaskellObject {
    /** Logger for this class. **/
    protected static final Logger logger = Logger.getLogger(Expression.class.getName());

    /** The last known type for this expression. */
    private Optional<Type> cachedType = Optional.empty();

    /**
     * Returns the latest type for this expression. If yet unknown it will be inferred,
     *
     * @param env The current Haskell environment.
     * @return The type for this usage of this expression.
     * @throws HaskellException if type inference fails in any way.
     */
    public final Type findType(Environment env) throws HaskellException {
        if (!this.cachedType.isPresent()) {
            Type type = this.inferType(env);
            this.setCachedType(type);
            return type;
        }

        return this.cachedType.get();
    }

    /**
     * Sets the cached type for this expression.
     * @param type The type to cache.
     */
    private final void setCachedType(final Type type) {
        if (!this.cachedType.isPresent() || this.cachedType.get() != type) {
            this.cachedType = Optional.ofNullable(type);
        }
    }

    public final void recalculateType(Environment env) throws HaskellException {
        this.setCachedType(this.inferType(env));
    }
    
    /**
     * Analyzes the type tree and infers the type for this usage of this expression
     *
     * @param env The current Haskell environment.
     * @return The type for this usage of this expression.
     * @throws HaskellException The type tree contains an application of an incompatible type.
     */
    protected abstract Type inferType(final Environment env) throws HaskellException; 

    /**
     * Returns the Haskell code for this expression.
     * @return The Haskell code for this expression.
     */
    public abstract String toHaskell();

    /**
     * @return A string representation of this Haskell expression.
     */
    @Override
    public abstract String toString();

    /**
     * @return a list of subexpressions, if any, or else an empty list.
     */
    public List<Expression> getChildren() {
        return ImmutableList.of();
    }
}
