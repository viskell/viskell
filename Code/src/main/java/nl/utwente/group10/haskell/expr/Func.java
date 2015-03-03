package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.Type;

/**
 * Abstract class for expressions that can be used with {@code Apply}.
 */
public abstract class Func extends Expr {
    /**
     * @param type The type of this function.
     */
    protected Func(final Type type) {
        super(type);
    }
}
