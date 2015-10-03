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
    static int tvOffset = -1;

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
        } else  if (a instanceof TypeVar) {
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
                // two type variable are unified by sharing the internal reference of (future) type instance   
                vb.shareInstanceOf(va);
            } else if (b instanceof FunType) {
            	// unifying a type variable with a function succeeds if it has no constraints.
            	FunType fb = (FunType) b;
            	if (va.hasConstraints()) {
                    TypeChecker.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                    throw new HaskellTypeError(String.format("%s ∉ constraints of %s", b, a), context, a, b);
            	} else {
            		va.setConcreteInstance(fb);
            	}
            } else {
            	ConstT tb = (ConstT) b;
                // Example: we have to unify (for example) α and Int.
                // Do so by stating that α must be Int, provided Int fits in α's typeclasses
                if (va.hasConstraint(tb)) {
                    va.setConcreteInstance(tb);
                } else {
                    TypeChecker.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                    throw new HaskellTypeError(String.format("%s ∉ constraints of %s", b, a), context, a, b);
                }
            }
        } else if (a instanceof ConcreteType && b instanceof TypeVar) {
            // Example: we have to unify Int and α.
            // Same as above, but mirrored.
            TypeChecker.unify(context, b, a);
        } else if (a instanceof ConstT && b instanceof ConstT) {
            // Example: we have to unify Int and Int.

            final ConstT ao = (ConstT) a;
            final ConstT bo = (ConstT) b;

            // If the constructor doesn't match, give up right away.
            // Example: trying to unify String and Int.
            if (!ao.getConstructor().equals(bo.getConstructor())) {
                TypeChecker.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                throw new HaskellTypeError(String.format("%s ⊥ %s", a, b), context, a, b);
            }

            // If the two types have different amounts of arguments, bail.
            // Example: trying to unify (,) Int Int and (,) Int Int Int
            if (ao.getArgs().length != bo.getArgs().length) {
                TypeChecker.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                throw new HaskellTypeError(String.format("%s ⊥ %s", a, b), context, a, b);
            }

            // Other than that, types can be unified if each of the arguments can be.
            for (int i = 0; i < ao.getArgs().length; i++) {
                TypeChecker.unify(context, ao.getArgs()[i], bo.getArgs()[i]);
            }
        } else if (a instanceof FunType && b instanceof FunType) {
        	FunType fa = (FunType) a;
        	FunType fb = (FunType) b;
            TypeChecker.unify(context, fa.getArgument(), fb.getArgument());
            TypeChecker.unify(context, fa.getResult(), fb.getResult());
        }
    }

    /**
     * Creates and returns a new {@code VarT} instance with a unique identifier.
     * @param prefix The base name of the type variable.
     * @param constraints Constraints for the new VarT.
     * @return A new variable type.
     */
    public static TypeVar makeVariable(final String prefix, final Set<TypeClass> constraints) {
        TypeChecker.tvOffset += 1;
        return new TypeVar(prefix, TypeChecker.tvOffset, constraints, null);
    }

    /**
     * Creates and returns a new {@code VarT} instance with a unique identifier.
     * @return A new variable type.
     */
    public static TypeVar makeVariable(final String prefix) {
        return makeVariable(prefix, new HashSet<TypeClass>());
    }
}
