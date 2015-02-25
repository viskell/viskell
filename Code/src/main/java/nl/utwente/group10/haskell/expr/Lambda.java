package nl.utwente.group10.haskell.expr;

import com.google.common.base.Joiner;
import nl.utwente.group10.haskell.type.FuncT;

import java.util.Arrays;

/**
 *
 */
public class Lambda extends Func {
    /**
     * The body of this lambda expression;
     */
    private final Expr body;

    /**
     * Names of arguments of this lambda expression;
     */
    private final String[] argNames;

    /**
     * @param type The type of this lambda.
     * @param body The body of this lambda. Can be any Expr object.
     * @param argNames The names of the arguments of this Lambda. The length of this array should be equal to the number
     *                 of arguments in the type minus one.
     */
    public Lambda(final FuncT type, final Expr body, final String[] argNames) {
        super(type);
        this.body = body;
        this.argNames = Arrays.copyOfRange(argNames, 0, type.getNumArgs());
    }

    @Override
    public final String toHaskell() {
        final StringBuilder out = new StringBuilder();
        out.append("(\\");

        for (final String arg : this.argNames) {
            out.append(arg).append(" ");
        }

        out.append("-> ");
        out.append(this.body.toHaskell());
        out.append(")");

        return out.toString();
    }

    @Override
    public final String toString() {
        return "Lambda{" +
                "type=" + this.getType() +
                ", argNames=" + Arrays.toString(this.argNames) +
                ", body='" + this.body + '\'' +
                '}';
    }
}
