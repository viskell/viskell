package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.HaskellObject;

/**
 * Binding for an Lambda object. Names a Haskell lambda.
 */
public class Binding extends HaskellObject {
    /**
     * The name the lambda is bound to.
     */
    private final String name;

    /**
     * The lambda that is bound.
     */
    private final Lambda lambda;

    /**
     * @param name The name to bind the lambda to.
     * @param lambda The lambda to bind.
     */
    public Binding(final String name, final Lambda lambda) {
        this.name = name;
        this.lambda = lambda;
    }

    /**
     * @return The name the lambda is bound to.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @return The lambda that is bound.
     */
    public final Lambda getLambda() {
        return this.lambda;
    }

    /**
     * @return A string representation of this Binding.
     */
    @Override
    public final String toString() {
        return "Binding{" +
                "name='" + this.name + '\'' +
                ", lambda=" + this.lambda +
                '}';
    }
}
