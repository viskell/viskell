package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;

import java.util.Arrays;

/**
 *
 */
public class Apply extends Expr {
    /**
     * The expression to apply arguments to.
     */
    private final Func func;

    /**
     * The result of this application.
     */
    private String result;

    /**
     * The arguments to apply.
     */
    private final Expr[] args;

    /**
     * @param func The expression to apply arguments to.
     * @param args The arguments to apply.
     * @throws HaskellException Invalid Haskell operation. See exception message for details.
     */
    public Apply(final Func func, final Expr ... args) throws HaskellException {
        super(((FuncT) func.getType()).getAppliedType());
        this.func = func;
        this.args = args.clone();
        this.result = "";
    }

    /**
     * @return The result of this application.
     */
    public final String getResult() {
        return this.result;
    }

    /**
     * @param result The calculated result of this application.
     */
    public final void setResult(final String result) {
        this.result = result;
    }

    /**
     * Returns the array of types for the given arguments.
     * @param args The array of arguments.
     * @return The array of types.
     */
    public static Type[] getArgTypes(final Expr[] args) {
        final Type[] types = new Type[args.length];

        for (int i = 0; i < args.length; i++) {
            types[i] = args[i].getType();
        }

        return types;
    }

    @Override
    public final String toHaskell() {
        final StringBuilder out = new StringBuilder();
        out.append("(").append(this.func.toHaskell()).append(")");

        for (final Expr arg : this.args) {
            out.append(" (").append(arg.toHaskell()).append(")");
        }

        return out.toString();
    }

    @Override
    public final String toString() {
        return "Apply{" +
                "func=" + this.func +
                ", args=" + Arrays.toString(this.args) +
                ", result='" + this.result + "'" +
                '}';
    }
}
