package nl.utwente.group10.haskell.type;

import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

/**
 * Abstract class for Haskell types. Provides an interface for common methods.
 */
public abstract class Type {
    /** Logger instance for types. */
    protected Logger logger = Logger.getLogger(Type.class.getName());

    /**
     * @return The readable representation of this type for in the UI.
     */
    public String prettyPrint() {
        return this.prettyPrint(0);
    }

    /**
     * @param The fixity of the context the type is shown in.
     * The fixity is small positive number derived from operator precedence (see also Section 4.4.2 of the Haskell language report)
     * @return The readable representation of this type for in the UI.
     */
    public abstract String prettyPrint(final int fixity);

    /**
     * @param The fixity of the context the type is shown in.
     * @param The list of types applied to this type.
     * @return the pretty representation of this type a list of of types 
     */
    protected String asTypeAppChain(final int fixity, final List<Type> args) {
        final StringBuilder out = new StringBuilder();
        out.append(this.prettyPrint(10));
        final ListIterator<Type> iter = args.listIterator(args.size());
        while (iter.hasPrevious()) {
            out.append(' ');
            out.append(iter.previous().prettyPrint(10));
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
        return this.getFresh(new TypeScope());
    }

    /**
     * @param scope The scope wherein related shared type variable are maintained
     * @return An equivalent deep copy of this type, using fresh type variables.
     */
    public abstract Type getFresh(final TypeScope scope);

    /**
     * @return The presence of the argument type variable some in this type.
     */
    public abstract boolean containsOccurenceOf(TypeVar tvar);

    @Override
    public abstract String toString();


    /**
     * @return a new type constructor
     * @param The name of type constructor.
     */
    public final static TypeCon con(String name) {
        return new TypeCon(name);
    }

    /**
     * @return a new tuple constructor
     * @param The arity of the tuple.
     */
    public final static TupleTypeCon tupleCon(int arity) {
        return new TupleTypeCon(arity);
    }

    /**
     * @return a new list constructor
     */
    public final static ListTypeCon listCon() {
        return new ListTypeCon();
    }

    /**
     * @return a new type from a applied type constructor
     * @param The name of type constructor.
     * @param List of type argument to constructor is applied too
     */
    public final static Type con(String name, Type... args) {
        Type t = new TypeCon(name);
        for (Type a : args) {
            t = new TypeApp(t, a);
        }

        return t;
    }

    /**
     * @return a new function type in a chain with all arguments
     * @param List of types 
     */
    public final static Type fun(Type... elems) {
        if (elems.length == 0) {
            throw new IllegalArgumentException("can not create an empty function type");
        }

        final int last = elems.length - 1;
        Type t = elems[last];
        for (int n = last - 1; n >= 0; n--) {
            t = new FunType(elems[n], t);
        }

        return t;
    }

    /**
     * @return a chained type application of all the arguments
     * @param List of types 
     */
    public final static Type app(Type... elems) {
        if (elems.length == 0) {
            throw new IllegalArgumentException("can not create an empty type applications");
        }

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
        return new TypeApp(listCon(), elem);
    }

    /**
     * @return a fully applied tuple type construction
     * @param the list of element types
     */
    public final static Type tupleOf(Type... elems) {
        Type t = tupleCon(elems.length);
        for (Type e : elems) {
            t = new TypeApp(t, e);
        }

        return t;
    }
}
