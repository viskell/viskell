package nl.utwente.group10.haskell.type;

import java.util.logging.Logger;

import nl.utwente.group10.haskell.HaskellObject;

/**
 * Abstract class for Haskell types. Provides an interface for common methods.
 */
public abstract class Type extends HaskellObject implements Comparable<Type> {
    /** Logger instance for types. */
    protected Logger logger = Logger.getLogger(Type.class.getName());

    /**
     * Prunes the type subtree and returns the resolved type for this type. The inferred type can be this type itself.
     *
     * During the unify progress of the Hindley-Milner algorithm, a type might be unified with another type. This means
     * that for some types a more concrete type can be used instead. For example, a variable type 'a' could be unified
     * with a constant type 'Int', in which case you could use 'Int' in all places where 'a' is used (and means the
     * same). Pruning makes sure that you are using a concrete type where possible and can be used to provide a better
     * output to end users.
     *
     * Pruning is not needed to provide correct type inference, but can be used without losing correct inference in the
     * future.
     *
     * @return The pruned type of this type.
     */
    public abstract Type prune();

    /**
     * @return The Haskell (type) representation of this type.
     */
    public abstract String toHaskellType();

    /**
     * @return An exactly alike deep copy of this type.
     */
    public abstract Type getFresh();

    @Override
    public abstract String toString();
}
