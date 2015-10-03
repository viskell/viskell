package nl.utwente.group10.haskell.type;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.ListIterator;
import java.util.logging.Logger;

import nl.utwente.group10.haskell.HaskellObject;

/**
 * Abstract class for Haskell types. Provides an interface for common methods.
 */
public abstract class Type extends HaskellObject {
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
     * The fixity is small positive number derived from operator precedence (see also Section 4.4.2 of the Haskell language report)
     * @return The Haskell (type) representation of this type.
     */
    public abstract String toHaskellType(final int fixity);

    /**
     * @param The fixity of the context the type is shown in.
     * @param The list of types applied to this type.
     * @return the Haskell (type) representation of this type a list of of types 
     */
    public String asTypeAppChain(final int fixity, final ArrayList<Type> args)
    {
        final StringBuilder out = new StringBuilder();
        final ListIterator<Type> iter = args.listIterator(args.size());
        while (iter.hasPrevious()) {
            out.append(iter.previous().toHaskellType(10));
            out.append(' ');
        }
        out.append(this.toHaskellType());
        if (fixity > 0) {
            return "(" + out.toString() + ")";
        }
        
        return out.toString();    
    }
    
    /**
     * @return An equivalent deep copy of this type, using fresh type variables.
     */
    public final Type getFresh() {
    	return this.getFreshInstance(new IdentityHashMap<TypeVar.TypeInstance, TypeVar>());
    }
    
    /**
     * @param A mapping from the old type variables (instances) to the new, the context wherein the fresh type is constructed.
     * @return An equivalent deep copy of this type, using fresh type variables.
     */
    protected abstract Type getFreshInstance(final IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh);
    
    /**
     * @return The presence of the argument type variable some in this type.
     */
    public abstract boolean containsOccurenceOf(TypeVar tvar);
    
    @Override
    public abstract String toString();
}
