package hindley;

import java.util.Optional;
import java.util.logging.Logger;

class HindleyMilner {
    private static Logger logger = Logger.getLogger(HindleyMilner.class.getName());
    static int tvOffset = 0;

    static void unify(Type t1, Type t2) {
        Type a = prune(t1);
        Type b = prune(t2);

        if (a instanceof TypeVar && !(a.equals(b))) {
            // Example: we have to unify (for example) α and Int.
            // Do so by stating that α must be Int.
            ((TypeVar) a).setInstance(Optional.of(b));
        } else if (a instanceof TypeOp && b instanceof TypeVar) {
            // Example: we have to unify Int and α.
            // Same as above, but mirrored.
            unify(b, a);
        } else if (a instanceof TypeOp && b instanceof TypeOp) {
            // Example: we have to unify Int and Int.

            TypeOp ao = (TypeOp) a;
            TypeOp bo = (TypeOp) b;

            // If the constructor doesn't match, give up right away.
            // Example: trying to unify String and Int.
            if (!ao.getConstructor().equals(bo.getConstructor())) {
                throw new RuntimeException(String.format("That's a no can do, pardner: %s ≠ %s", a, b));
            }

            // If the two types have different amounts of arguments, bail.
            // Example: trying to unify (,) Int Int and (,) Int Int Int
            if (ao.getArgs().length != bo.getArgs().length) {
                throw new RuntimeException(String.format("No way, José: %s ≠ %s", a, b));
            }

            // Other than that, types can be unified if each of the arguments can be.
            for (int i = 0; i < ao.getArgs().length; i++) {
                unify(ao.getArgs()[i], bo.getArgs()[i]);
            }
        }
    }

    static Type prune(Type t) {
        if (t instanceof TypeVar) {
            TypeVar tv = (TypeVar) t;
            if (tv.getInstance().isPresent()) {
                Type instance = prune(tv.getInstance().get());
                tv.setInstance(Optional.of(instance));

                return instance;
            }
        }

        return t;
    }

    static TypeVar makeTypeVar() {
        String name;

        if (tvOffset <= 25) {
            name = String.valueOf((char) ('α' + tvOffset));
        } else {
            name = Integer.toString(tvOffset);
        }

        tvOffset += 1;
        return new TypeVar(name);
    }
}
