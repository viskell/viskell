package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.FuncT;

/**
 * Lambda expression. Currently not very well supported, only type checking available.
 */
public class Lambda extends Func {
    /**
     * The String representation of the body of this lambda expression.
     */
    private final String body;

    /**
     * @param type The type of this lambda.
     * @param body The String representation of the body of this lambda expression.
     */
    public Lambda(final FuncT type, final String body) {
        super(type);
        this.body = body;
    }

    @Override
    public final String toHaskell() {
        return this.body;
    }

    @Override
    public final String toString() {
        return "Lambda{" +
                "type=" + this.getType() +
                ", body='" + this.body + '\'' +
                '}';
    }
}
