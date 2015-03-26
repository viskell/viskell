package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.HaskellObject;

import java.util.logging.Logger;

/**
 * Abstract class for Haskell types. Provides an interface for common methods.
 *
 * Types should be immutable. Also, a Type subclass instance that represents the same in Haskell as another instance of
 * the same class should be equal, that is {@code this.equals(that)} should return {@code true}.
 */
public abstract class Type extends HaskellObject {
    /** Logger instance for types. */
    protected Logger logger = Logger.getLogger(Type.class.getName());

    /**
     * Prunes the type subtree and returns the resolved type for this type. The resolved type can be this type itself.
     * @return The pruned type of this type.
     */
    public abstract Type prune();

    /**
     * @return The Haskell (type) representation of this type.
     */
    public abstract String toHaskellType();

    @Override
    public abstract String toString();
}
