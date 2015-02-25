package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.HaskellObject;

/**
 * Binding for an Expr object. Names a Haskell expression.
 */
public class Binding extends HaskellObject {
    /**
     * The name the expression is bound to.
     */
    private final String name;

    /**
     * The expression that is bound.
     */
    private final Expr expr;

    /**
     * @param name The name to bind the expression to.
     * @param expr The expression to bind.
     */
    public Binding(final String name, final Expr expr) {
        this.name = name;
        this.expr = expr;
    }

    /**
     * @return The name the expression is bound to.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @return The expression that is bound.
     */
    public final Expr getExpr() {
        return this.expr;
    }

    /**
     * @return A string representation of this Binding.
     */
    @Override
    public final String toString() {
        return "Binding{" +
                "name='" + this.name + '\'' +
                ", expr=" + this.expr +
                '}';
    }
}
