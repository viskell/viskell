package nl.utwente.group10.haskell.type;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Expr;

/**
 * Implementation of a typechecker for a simplified variant of Haskell.
 */
public final class TypeChecker {
    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(TypeChecker.class.getName());

    /**
     * Offset for the creation of variable types.
     */
    static int tvOffset = 0;

    static {
        TypeChecker.logger.setLevel(Level.WARNING);
        // Changing this to Level.INFO will show debug messages.
    }
    
    /**
     * Private constructor - methods in this class are static.
     */
    private TypeChecker() {
    }

    public static void unify(final Type t1, final Type t2) throws HaskellTypeError {
        unify(null, t1, t2);
    }

    public static void unify(final Expr context, final Type a, final Type b) throws HaskellTypeError {
        
        TypeChecker.logger.info(String.format("Unifying types %s and %s for context %s", a, b, context));

        if (a.equals(b)) {
        	// for identical types unifying is trivial
        } else if (a instanceof TypeVar) {
            TypeVar va = (TypeVar) a;

            // First, prevent ourselves from going into an infinite loop
            if (b.containsOccurenceOf(va)) {
                TypeChecker.logger.info(String.format("Recursion in types %s and %s for context %s", a, b, context));
                throw new HaskellTypeError(String.format("%s ∈ %s", a, b), context, a, b);
            }

            if (va.hasInstance()) {
            	// if a type variable has been instantiated already then we can just unify b with a concrete type of a
            	TypeChecker.unify(context, va.getInstantiatedType(), b);
            } else if (b instanceof TypeVar) {
                TypeVar vb = (TypeVar) b;
                
                if (vb.hasInstance()) {
                    // with type variable b instantiated continue with unifying type variable a with the concrete type of b
                    TypeChecker.unify(context, va, vb.getInstantiatedType());
                } else {
                    // two plain type variable are unified by sharing the internal reference of (future) type instance   
                    vb.shareInstanceOf(va);
                }
            } else if (b instanceof FunType) {
            	// unifying a type variable with a function succeeds if it has no constraints, because function instances are not supported (yet?).
            	FunType fb = (FunType) b;
            	if (va.hasConstraints()) {
                    TypeChecker.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                    throw new HaskellTypeError(String.format("%s ∉ constraints of %s", b, a), context, a, b);
            	} else {
            		va.setConcreteInstance(fb);
            	}
            } else if (b instanceof TypeApp) {
                // unifying a type variable with a type application succeeds (for now until nested type instance are added) if it has no constraints.
                TypeApp tb = (TypeApp) b;
                if (va.hasConstraints()) {
                    TypeChecker.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                    throw new HaskellTypeError(String.format("%s ∉ constraints of %s", b, a), context, a, b);
                } else {
                    va.setConcreteInstance(tb);
                }
                
            } else {
            	TypeCon tb = (TypeCon) b;
                // Example: we have to unify (for example) α and Int.
                // Do so by stating that α must be Int, provided Int fits in α's typeclasses
                if (va.hasConstraint(tb)) {
                    va.setConcreteInstance(tb);
                } else {
                    TypeChecker.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                    throw new HaskellTypeError(String.format("%s ∉ constraints of %s", b, a), context, a, b);
                }
            }
        } else if (b instanceof TypeVar && a instanceof ConcreteType) {
            // Example: we have to unify Int and α.
            // Same as above, but mirrored.
            TypeChecker.unify(context, b, a);
        } else if (a instanceof TypeCon && b instanceof TypeCon) {
            final TypeCon ca = (TypeCon) a;
            final TypeCon cb = (TypeCon) b;
            // unification of type constructor is just name equality
            if (! ca.getName().equals(cb.getName()))
            {
                TypeChecker.logger.info(String.format("Mismatching TypeCon %s and %s for context %s", a, b, context));
                throw new HaskellTypeError(String.format("%s ⊥ %s", a, b), context, a, b);
            }
        } else if (a instanceof FunType && b instanceof FunType) {
            // Unifying function types is pairwise unification of its argument and result. 
        	FunType fa = (FunType) a;
        	FunType fb = (FunType) b;
            TypeChecker.unify(context, fa.getArgument(), fb.getArgument());
            TypeChecker.unify(context, fa.getResult(), fb.getResult());
        } else if (a instanceof TypeApp && b instanceof TypeApp) {
            // Unifying type applications is pairwise unification of its typeFun and typeArg. 
            TypeApp ta = (TypeApp) a;
            TypeApp tb = (TypeApp) b;
            TypeChecker.unify(context, ta.getTypeFun(), tb.getTypeFun());
            TypeChecker.unify(context, ta.getTypeArg(), tb.getTypeArg());
        } else {
            // Running out of things that can be unified, so bail out with a type error.
            TypeChecker.logger.info(String.format("Given up to unify types %s and %s for context %s", a, b, context));
            throw new HaskellTypeError(String.format("%s ⊥ %s", a, b), context, a, b);
        }
    }

    /**
     * Creates and returns a new {@code VarT} instance with a unique identifier.
     * @param prefix The base name of the type variable.
     * @param constraints Constraints for the new VarT.
     * @return A new variable type.
     */
    public static TypeVar makeVariable(final String prefix, final Set<TypeClass> constraints) {
        return new TypeVar(prefix, TypeChecker.tvOffset++, constraints, null);
    }

    /**
     * Creates and returns a new {@code VarT} instance with a unique identifier.
     * @return A new variable type.
     */
    public static TypeVar makeVariable(final String prefix) {
        return makeVariable(prefix, new HashSet<TypeClass>());
    }
}
