package nl.utwente.group10.haskell.hindley;

import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.VarT;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementation of the Hindley-Milner type system for Haskell types and expressions.
 */
public final class HindleyMilner {
    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(HindleyMilner.class.getName());

    /**
     * Offset for the creation of variable types.
     */
    static int tvOffset = 0;

    /**
     * Private constructor - methods in this class are static.
     */
    private HindleyMilner() {
    }

    public static void unify(final Type t1, final Type t2) throws HaskellTypeError {
        unify(null, t1, t2);
    }

    public static void unify(final Expr context, final Type t1, final Type t2) throws HaskellTypeError {
        final Type a = t1.prune();
        final Type b = t2.prune();

        HindleyMilner.logger.info(String.format("Unifying types %s and %s for context %s", t1, t2, context));

        if (a instanceof VarT && !a.equals(b)) {
            // Example: we have to unify (for example) α and Int.
            // Do so by stating that α must be Int.
            ((VarT) a).setInstance(b);
        } else if (a instanceof ConstT && b instanceof VarT) {
            // Example: we have to unify Int and α.
            // Same as above, but mirrored.
            HindleyMilner.unify(context, b, a);
        } else if (a instanceof ConstT && b instanceof ConstT) {
            // Example: we have to unify Int and Int.

            final ConstT ao = (ConstT) a;
            final ConstT bo = (ConstT) b;

            // If the constructor doesn't match, give up right away.
            // Example: trying to unify String and Int.
            if (!ao.getConstructor().equals(bo.getConstructor())) {
                throw new HaskellTypeError(String.format("Sadly, these types are not compatible: %s ≠ %s", a, b), context);
            }

            // If the two types have different amounts of arguments, bail.
            // Example: trying to unify (,) Int Int and (,) Int Int Int
            if (ao.getArgs().length != bo.getArgs().length) {
                throw new HaskellTypeError(String.format("Sadly, these types are not compatible: %s ≠ %s", a, b), context);
            }

            // Other than that, types can be unified if each of the arguments can be.
            for (int i = 0; i < ao.getArgs().length; i++) {
                HindleyMilner.unify(context, ao.getArgs()[i], bo.getArgs()[i]);
            }
        }
    }

    /**
     * Creates and returns a new {@code VarT} instance with a unique identifier.
     * @return A new variable type.
     */
    public static VarT makeVariable() {
        final String name;

        name = HindleyMilner.tvOffset <= 25
                ? String.valueOf((char) ('α' + HindleyMilner.tvOffset))
                : Integer.toString(HindleyMilner.tvOffset);

        HindleyMilner.tvOffset += 1;

        return new VarT(name);
    }
}
