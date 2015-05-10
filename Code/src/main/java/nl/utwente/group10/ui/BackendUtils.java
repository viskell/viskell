package nl.utwente.group10.ui;

import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;

/**
 * Provides some extra utility the front end needs, but does not really belong in the Haskell backend.
 */
public class BackendUtils {
    /**
     * @param steps
     *            Amount of steps to dive
     * @return The First argument in this function with steps more depth than
     *         this function.
     *
     *         example: (a -> (b -> (c -> d))).dive(2) = b
     *
     *         If it is impossible to dive the amount of steps provided, it
     *         returns the closest result to that.
     */
    public static Type dive(ConstT function, int steps) {
        if (steps == 1) {
            return function.getArgs()[0];
        } else if (steps > 1) {
            Type next = function.getArgs()[1];
            if (steps > 1) {
                if (next instanceof ConstT) {
                    return dive(((ConstT) next), steps - 1);
                }
            }
        }
        return function;
    }
}
