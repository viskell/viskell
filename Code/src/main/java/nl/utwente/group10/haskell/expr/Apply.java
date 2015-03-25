package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;

/**
 * Lazy application of an argument to a function.
 */
public class Apply extends Expr {
    /**
     * The expression to apply the argument to.
     */
    private final Expr func;

    /**
     * The argument to apply.
     */
    private final Expr arg;

    /**
     * Applies an argument to a function to produce a new expression. The application is lazy, the type needs to be
     * analyzed before it can be resolved.
     *
     * @param func The expression to apply argument to.
     * @param arg The argument to apply.
     */
    public Apply(final Expr func, final Expr arg) {
        this.func = func;
        this.arg = arg;
    }

    @Override
    public final Type analyze(final Env env, final GenSet genSet) throws HaskellTypeError {
        final Type funcType = func.analyze(env, genSet);
        final Type argType = func.analyze(env, genSet);
        final Type resType = HindleyMilner.makeVariable();


        // Rule [App]:
        // IFF  the type of our function is a -> b and the type of our arg is a
        // THEN the type of our result is b
        HindleyMilner.unify(this, funcType, new FuncT(argType, resType));

        return resType;
    }

    @Override
    public final String toHaskell() {
        return String.format("(%s %s)", this.func, this.arg);
    }

    @Override
    public final String toString() {
        return String.format("(%s %s)", this.func, this.arg);
    }
}
