package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;

import java.util.*;

/**
 * Represents a user-defined Haskell function. Holds arguments which can be used for building the function.
 */
public class Function extends Expr {

    /**
     * An argument for a function. A FunctionArgument can be used as a variable in a function definition.
     */
    public static class FunctionArgument extends Expr {
        /** The type for this argument. */
        private Type type;

        /** The name of this argument. Will be randomly generated to a unique value. */
        private String name;

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
        public Type analyze(Env env, GenSet genSet) throws HaskellException {
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
    private Expr expr;

    /**
     * @param expr The expression that forms the base of this function.
     */
    public Function(Expr expr, FunctionArgument ... arguments) {
        this.arguments = Arrays.asList(arguments);
        this.expr = expr;
    }

    /**
     * @return An ordered array of the arguments for this function.
     */
    public FunctionArgument[] getArguments() {
        FunctionArgument[] args = new FunctionArgument[this.arguments.size()];
        return this.arguments.toArray(args);
    }

    @Override
    public Type analyze(Env env, GenSet genSet) throws HaskellException {
        Type type = this.expr.getType(env).prune().getFresh();

        for (int i = this.arguments.size(); i > 0; i--) {
            type = new FuncT(this.arguments.get(i - 1).getType(env).getFresh(), type);
        }

        this.setCachedType(type);

        return type;
    }

    @Override
    public String toHaskell() {
        StringBuilder out = new StringBuilder();

        if (!this.arguments.isEmpty()) {
            out.append("\\");

            for (FunctionArgument argument : this.arguments) {
                out.append(" ").append(argument.toHaskell());
            }

            out.append(" -> ");
            out.append(this.expr.toHaskell());
        } else {
            out.append(this.expr.toHaskell());
        }

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
