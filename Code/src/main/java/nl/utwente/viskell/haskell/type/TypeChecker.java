package nl.utwente.viskell.haskell.type;

import nl.utwente.viskell.haskell.expr.Expression;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a typechecker for a simplified variant of Haskell.
 */
public final class TypeChecker {
    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(TypeChecker.class.getName());

    static {
        TypeChecker.logger.setLevel(Level.WARNING);
        // Changing this to Level.INFO will show debug messages.
    }
    
    /**
     * Private constructor - methods in this class are static.
     */
    private TypeChecker() {
    }

    public static void unify(final Expression context, final Type a, final Type b) throws HaskellTypeError {
        TypeChecker.unify(context.toString(), a, b);
    }
    
    public static void unify(final String context, final Type a, final Type b) throws HaskellTypeError {
        
        TypeChecker.logger.info(String.format("Unifying types %s and %s for context %s", a, b, context));

        if (a.equals(b)) {
            // for identical types unifying is trivial
        } else if (a instanceof TypeVar) {
            TypeVar va = (TypeVar) a;

            // First, prevent ourselves from going into an infinite loop
            if (b.containsOccurenceOf(va)) {
                TypeChecker.logger.info(String.format("Recursion in types %s and %s for context %s", a, b, context));
                throw new HaskellTypeError(String.format("%s ∈ %s in context of %s", a, b, context));
            }

            if (va.hasConcreteInstance()) {
                // if a type variable has been instantiated already then we can just unify b with a concrete type of a
                TypeChecker.unify(context, va.getInstantiatedType(), b);
            } else if (b instanceof TypeVar) {
                TypeVar vb = (TypeVar) b;
                
                if (vb.hasConcreteInstance()) {
                    // with type variable b instantiated continue with unifying type variable a with the concrete type of b
                    TypeChecker.unify(context, va, vb.getInstantiatedType());
                } else {
                    // two plain type variable are unified by sharing the internal reference of (future) type instance   
                    vb.unifyWith(va);
                }
            } else if (b instanceof ConcreteType) {
                ConcreteType tb = (ConcreteType) b;
                // check that the type satisfy the constraints
                TypeChecker.satisfyConstraints(tb, va.getConstraints(), context);
                // then make the type variable instantiated by this concrete type 
                va.setConcreteInstance(tb);
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
                throw new HaskellTypeError(String.format("%s ⊥ %s in context of %s", a, b, context));
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
            throw new HaskellTypeError(String.format("%s ⊥ %s in context of %s", a, b, context));
        }
    }

    /**
     * Check and enforce that a type matches a set of type class constraints.
     * 
     * @param type A concrete type which is affected by the constraints
     * @param constraints The set of constraint that need to be satisfied.
     * @param context the expression string to use as context in errors. 
     * @throws HaskellTypeError if the constraints can no be satisfied by this type.
     */
    private static void satisfyConstraints(Type type, ConstraintSet constraints, String context) throws HaskellTypeError {
        if (! constraints.hasConstraints()) {
            // empty constraints are always satisfied.
            return;
        }
        
        if (type instanceof TypeVar) {
            TypeVar tv = (TypeVar) type;
            if (tv.hasConcreteInstance()) {
                // just constrain the concrete instantiation
                TypeChecker.satisfyConstraints(tv.getInstantiatedType(), constraints, context);
                return;
            } else {
                // add extra constraint for this type variable
                tv.introduceConstrainst(constraints);
                return;
            }
        } else if (type instanceof TypeCon) {
            TypeCon tc = (TypeCon)type;
            // directly check if all constraints are satisfied 
            if (constraints.allConstraintsMatch(tc)) {
                return;
            }
        } else if (type instanceof TypeApp) {
            TypeApp ta = (TypeApp)type;
            List<Type> chain = ta.asFlattenedAppChain();
            Type ctype = chain.remove(0);
            // check if the head of a type application chain is a known type constructor 
            if (ctype instanceof TypeCon) {
                TypeCon tc = (TypeCon)ctype;
                if (constraints.allConstraintsMatch(tc)) {
                    // also for all type arguments add implied constraint as needed
                    int arity = chain.size();
                    List<ConstraintSet> argConstraints = constraints.getImpliedArgConstraints(tc, arity);
                    for (int i = 0; i < arity; i++) {
                        TypeChecker.satisfyConstraints(chain.get(i), argConstraints.get(i), context);
                    }
                    // all satisfied, done
                    return;
                }
            }
        }
        
        // for now, constraining other types will fail.
        TypeChecker.logger.info(String.format("Unable to unify types %s with constraints %s for context %s", type, constraints, context));
        throw new HaskellTypeError(String.format("%s ∉ constraints of %s in context of %s", type, constraints, context));
    }

}
