package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.HaskellObject;
import nl.utwente.group10.haskell.type.Type;

/**
 * An expression in Haskell.
 */
public abstract class Expr extends HaskellObject {
    /**
     * Type of this expression. Used for in-Java type checking.
     */
    private final Type type;

    /**
     * @param type Type of this expression.
     */
    protected Expr(final Type type) {
        this.type = type;
    }

    /**
     * @return The type of this Haskell expression.
     */
    public final Type getType() {
        return this.type;
    }

    /**
     * Returns the Haskell code for this expression.
     * @return The Haskell code for this expression.
     */
    public abstract String toHaskell();

    /**
     * @return A string representation of this Haskell expression.
     */
    @Override
    public abstract String toString();
}
