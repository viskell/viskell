package nl.utwente.viskell.haskell.expr;

import com.google.common.collect.Lists;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Represents a user-defined Haskell function. Holds arguments which can be used for building the function.
 */
public class Function extends Expression {

    /**
     * An argument for a function. A FunctionArgument can be used as a variable in a function definition.
     */
    public static class FunctionArgument extends Expression {
        /** The type for this argument. */
        private final Type type;

        /** The name of this argument. Will be randomly generated to a unique value. */
        private final String name;

        /**
         * Constructor for a FunctionArgument with an explicit type.
         *
         * @param type The required type for this argument.
         */
        public FunctionArgument(Type type) {
            this.type = type;
            this.name = String.format("var_%s", UUID.randomUUID().toString().substring(0, 8));
        }

        @Override
        public Type inferType() {
            return this.type;
        }

        @Override
        public String toHaskell() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /** The arguments for this function. */
    private List<FunctionArgument> arguments;

    /** The expression that forms the base of this function. */
    private Expression expr;

    /**
     * @param expr The expression that forms the base of this function.
     * @param arguments Expressions that represent the function's arguments.
     */
    public Function(Expression expr, FunctionArgument ... arguments) {
        this(expr, Arrays.asList(arguments));
    }

    /**
     * @param expr The expression that forms the base of this function.
     * @param arguments Expressions that represent the function's arguments.
     */
    public Function(Expression expr, List<FunctionArgument> arguments) {
        this.expr = expr;
        this.arguments = arguments;
    }

    /**
     * @return An ordered array of the arguments for this function.
     */
    public FunctionArgument[] getArguments() {
        FunctionArgument[] args = new FunctionArgument[this.arguments.size()];
        return this.arguments.toArray(args);
    }

    @Override
    protected Type inferType() throws HaskellTypeError {
        Type ftype = this.expr.inferType().getFresh();

        for (Expression arg : Lists.reverse(this.arguments)) {
            ftype = Type.fun(arg.inferType().getFresh(), ftype);
        }

        return ftype;
    }

    @Override
    public String toHaskell() {
        StringBuilder out = new StringBuilder();
        out.append("(");

        if (!this.arguments.isEmpty()) {
            out.append("\\");

            for (FunctionArgument argument : this.arguments) {
                out.append(" ").append(argument.toHaskell());
            }

            out.append(" -> ");
        }

        out.append(this.expr.toHaskell());
        out.append(")");

        return out.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        if (!this.arguments.isEmpty()) {
            out.append("λ");

            for (FunctionArgument argument : this.arguments) {
                out.append(" ").append(argument.toString());
            }

            out.append(" -> ");
        }

        out.append(this.expr.toString());

        return out.toString();
    }
}
