package nl.utwente.group10.haskell.type;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.ListIterator;
import java.util.Set;
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
        out.append(this.toHaskellType(10));
        final ListIterator<Type> iter = args.listIterator(args.size());
        while (iter.hasPrevious()) {
            out.append(' ');
            out.append(iter.previous().toHaskellType(10));
        }
        if (fixity > 1) {
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
    
    /**
     * The list type constructor.
     */
    public final static TypeCon ListCon = new ListTypeCon();
    
    /**
     * @return a new type variable constructed from a name and optionally a number of class constraint 
     * @param name Identifier for this type variable.
     * @param constraints The constraints for this type
     */
    public final static TypeVar var(final String name, TypeClass... constraints) {
        return new TypeVar(name, constraints);
    }
    
    /**
     * @return a new type variable conctructed from a name and a class constraint set. 
     * @param name Identifier for this type variable.
     * @param constraints The set of constraints for this type
     */
    public final static TypeVar var(final String name, final Set<TypeClass> constraints) {
        return new TypeVar(name, 0, constraints, null);
    }

    /**
     * @return a new type constructor
     * @param The name of type constructor.
     */
    public final static TypeCon con(String name) {
        return new TypeCon(name);
    }

    /**
     * @return a new type from a applied type constructor
     * @param The name of type constructor.
     * @param List of type argument to constructo is applied too
     */
    public final static Type con(String name, Type... args) {
        Type t = new TypeCon(name);
        for (Type a : args) {
            t = new TypeApp (t, a);
        }
        return t;
    }
    
    /**
     * @return a new function type in a chain with all arguments
     * @param List of types 
     */
    public final static Type fun(Type... elems) {
        final int last = elems.length-1;
        Type t = elems[last];
        for (int n = last-1; n >= 0; n--) {
            t = new FunType (elems[n], t);
        }
        return t;
    }
    
    /**
     * @return a chained type application of all the arguments
     * @param List of types 
     */
    public final static Type app(Type... elems) {
        Type t = elems[0];
        for (int n = 1; n < elems.length; n++) {
            t = new TypeApp(t, elems[n]);
        }
        return t;
    }
    
    /**
     * @return an applied list type construction
     * @param the element type
     */
    public final static Type listOf(Type elem) {
        return new TypeApp(ListCon, elem);
    }
    
    /**
     * @return a fully applied tuple type construction
     * @param the list of element types
     */
    public final static Type tupleOf(Type... elems) {
        Type t = new TupleTypeCon(elems.length);
        for (Type e : elems) {
            t = new TypeApp(t, e);
        }
        return t;
    }
}
