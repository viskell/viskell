package nl.utwente.viskell.haskell.expr;

import com.google.common.collect.ImmutableList;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeChecker;
import nl.utwente.viskell.haskell.type.TypeScope;

import java.util.List;

/**
 * Lazy application of an argument to a function.
 */
public class Apply extends Expression {
    /**
     * The expression to apply the argument to.
     */
    private final Expression func;

    /**
     * The argument to apply.
     */
    private final Expression arg;

    /**
     * Applies an argument to a function to produce a new expression. The application is lazy, the type needs to be
     * analyzed before there is certainty about the validity of this application and the resulting type.
     *
     * @param func The expression to apply argument to.
     * @param arg The argument to apply.
     */
    public Apply(final Expression func, final Expression arg) {
        this.func = func;
        this.arg = arg;
    }

    @Override
    protected final Type inferType() throws HaskellTypeError {
        final Type funcType = func.inferType();
        final Type argType = arg.inferType();
        final Type resType = TypeScope.unique("b");

        // Rule [App]:
        // IFF  the type of our function is a -> b and the type of our arg is a
        // THEN the type of our result is b
        TypeChecker.unify(this, funcType, Type.fun(argType, resType));

        return resType;
    }

    @Override
    public final String toHaskell() {
        return String.format("(%s %s)", this.func.toHaskell(), this.arg.toHaskell());
    }

    @Override
    public final String toString() {
        return String.format("(%s %s)", this.func.toString(), this.arg.toString());
    }

    @Override
    public final List<Expression> getChildren() {
        return ImmutableList.of(func, arg);
    }
}
