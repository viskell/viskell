package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.ghcj.HaskellException;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;

import java.util.Arrays;

/**
 * A type of expression that is a lazy application of a function with certain arguments.
 */
public class Apply extends Expr {
    /**
     * The expression to apply arguments to.
     */
    private final Func func;

    /**
     * The arguments to apply.
     */
    private final Expr[] args;

    /**
     * Constructs a new function application object. The resulting type will be calculated based on the given arguments.
     * The type of the arguments is validated, and an exception is thrown when one of the arguments is incorrect.
     *
     * @param func The expression to apply arguments to.
     * @param args The arguments to apply.
     * @throws HaskellException Invalid Haskell operation. See exception message for details.
     */
    public Apply(final Func func, final Expr ... args) throws HaskellException {
        super(((FuncT) func.getType()).getAppliedType());
        this.func = func;
        this.args = args.clone();
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
                '}';
    }
}
