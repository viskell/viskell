package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;

import java.util.Arrays;

/**
 * A type of expression that is a lazy application of a function with certain arguments.
 */
public class Apply extends Func {
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
     * @throws HaskellTypeError The types of the expressions are not valid for the given function.
     */
    public Apply(final Func func, final Expr ... args) throws HaskellTypeError {
        super(((FuncT) func.getType()).getAppliedType(func, Apply.getArgTypes(args)));
        this.func = func;
        this.args = args;
    }

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
        out.append(this.func.toHaskell());

        for (final Expr arg : this.args) {
            out.append(" ").append(arg.toHaskell());
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
