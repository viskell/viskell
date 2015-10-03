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
     * @return The Haskell (type) representation of this type.
     */
    public String toHaskellType() {
    	return this.toHaskellType(0);
    }

    /**
     * @param The fixity of the context the type is shown in.
     * @return The Haskell (type) representation of this type.
     */
    public abstract String toHaskellType(final int fixity);

    /**
     * @return An exactly alike deep copy of this type.
     */
    public abstract Type getFresh();

    
    /**
     * @param The fixity of the context the type is shown in.
     * @return The presence of the argument type variable some in this type.
     */
    public abstract boolean containsOccurenceOf(TypeVar tvar);
    
    @Override
    public abstract String toString();
}
