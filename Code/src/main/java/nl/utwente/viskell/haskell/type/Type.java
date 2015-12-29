package nl.utwente.viskell.haskell.type;

import com.google.common.base.Joiner;

import java.util.List;
import java.util.Optional;

/**
 * Abstract class for Haskell types. Provides an interface for common methods.
 */
public abstract class Type {

	/** @return The number of arguments that can be applied to a value of this type. */
	public int countArguments() {
		int count = 0;
		Type type = this;
		while (type instanceof FunType) {
			count++;
			type = ((FunType)type).getResult();
		}
		
		return count;
	}
	
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
    protected String prettyPrintAppChain(final int fixity, final List<Type> args) {
        final StringBuilder out = new StringBuilder();
        out.append(this.prettyPrint(10));
        out.append(' ');
        out.append(Joiner.on(' ').join(args.stream().map(a -> a.prettyPrint(10)).iterator()));
        
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

    /** @return The type with all instantiated type variable replaced by concrete types. */
    public abstract Type getConcrete();
    
    /**
     * @return The presence of the argument type variable some in this type.
     */
    public abstract boolean containsOccurenceOf(TypeVar tvar);

    @Override
    public abstract String toString();

    /** Ensures that this type stays maximal polymorphic by marking all internal type variables rigid */
    public void enforcePolymorphism() {
        for (TypeVar.TypeInstance tvi : TypeScope.gatherAllTypeVarInsts(this)) {
            tvi.makeRigid();
        }
    }
    
    /**
     * Attempts to produce a concrete type with all type variables instantiated.
     * @param backupFiller the type to use when no type can be found using type class defaulting.
     * @return a type without any polymorphism, if successful.
     */
    public Optional<Type> defaultedConcreteType(ConcreteType backupFiller) {
        Type ftype = this.getFresh();
        try {
            for (TypeVar.TypeInstance tvi : TypeScope.gatherAllTypeVarInsts(ftype)) {
                tvi.defaultOrElse(backupFiller);
            }
            Type ctype = ftype.getConcrete();
            TypeChecker.unify("defaulting validation", ctype.getFresh(), this.getFresh());
            return Optional.of(ctype);
        } catch (HaskellTypeError e) {
            return Optional.empty();
        }
    }

    /**
     * @return a new type constructor
     * @param The name of type constructor.
     */
    public final static TypeCon con(String name) {
        if ("[]".equals(name)) {
            return new ListTypeCon();
        }
        
        if ("()".equals(name)) {
            return new TupleTypeCon(0);
        }
        
        if (name.startsWith("(,")) {
            return new TupleTypeCon(name.length()-1);
        }
        
        return new TypeCon(name);
    }

    /**
     * @return a new type from a applied type constructor
     * @param The name of type constructor.
     * @param List of type argument to constructor is applied too
     */
    public final static Type con(String name, Type... args) {
        Type t = Type.con(name);
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
        return new TypeApp(new ListTypeCon(), elem);
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
