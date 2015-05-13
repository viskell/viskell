package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a user-defined Haskell function. Holds arguments which can be used for building the function.
 */
public class Function extends Expr {

    /**
     * An argument for a function. Can be used as a variable in a function definition.
     */
    public class FunctionArgument extends Expr {

        /** The function this argument is for. */
        private Function function;

        /**
         * @param function The function this argument is for.
         */
        protected FunctionArgument(Function function) {
            this.function = function;
        }

        /**
         * @return The 0-indexed position of this argument in the function definition.
         */
        public int getPosition() {
            return this.function.arguments.indexOf(this);
        }

        @Override
        public Type analyze(Env env, GenSet genSet) throws HaskellException {
            return HindleyMilner.makeVariable(); // TODO Make sure that this is correctly unified and/or make this editable.
        }

        @Override
        public String toHaskell() {
            return String.format("%s%d", Function.ARG_PREFIX, this.getPosition());
        }

        @Override
        public String toString() {
            return String.format("%s%d", Function.ARG_PREFIX, this.getPosition());
        }
    }

    /** The prefix for function argument names. */
    protected static final String ARG_PREFIX = "arg";

    /** The arguments for this function. */
    private List<FunctionArgument> arguments;

    /** The expression that forms the base of this function. */
    private Optional<Expr> expr;

    /**
     * @param expr The expression that forms the base of this function.
     */
    public Function(Expr expr) {
        this.arguments = new ArrayList<>();
        this.expr = Optional.ofNullable(expr);
    }

    /**
     */
    public Function() {
        this(null);
    }

    /**
     * Sets the expression for this function.
     * @param expr The expression to set.
     */
    public void setExpr(Expr expr) {
        this.expr = Optional.ofNullable(expr);
    }

    /**
     * @return An ordered array of the arguments for this function.
     */
    public FunctionArgument[] getArguments() {
        FunctionArgument[] args = new FunctionArgument[this.arguments.size()];
        return this.arguments.toArray(args);
    }

    /**
     * Adds a new argument to this function.
     * @return The new argument.
     */
    public FunctionArgument addArgument() {
        FunctionArgument arg = new FunctionArgument(this);
        this.arguments.add(arg);
        return arg;
    }

    /**
     * Removes the given argument from this function. The argument is not removed from any expressions within this
     * function.
     * @param arg The argument to remove.
     */
    public void removeArgument(FunctionArgument arg) {
        this.arguments.remove(arg);
    }

    @Override
    public Type analyze(Env env, GenSet genSet) throws HaskellException {
        Type type;

        if (this.expr.isPresent()) {
            type = this.expr.get().analyze(env, genSet);
        } else {
            type = HindleyMilner.makeVariable();

            for (int i = this.arguments.size(); i > 0; i--) {
                type = new FuncT(this.arguments.get(i-1).analyze(env, genSet), type);
            }
        }

        return type;
    }

    @Override
    public String toHaskell() {
        StringBuilder out = new StringBuilder();

        if (this.expr.isPresent()) {
            out.append("\\");

            for (FunctionArgument argument : this.arguments) {
                out.append(" ").append(argument.toHaskell());
            }

            out.append(" = ");
            out.append(this.expr.get().toHaskell());
        }

        return out.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        if (this.expr.isPresent()) {
            out.append("\\");

            for (FunctionArgument argument : this.arguments) {
                out.append(" ").append(argument.toString());
            }

            out.append(" = ");
            out.append(this.expr.get().toString());
        }

        return out.toString();
    }
}
