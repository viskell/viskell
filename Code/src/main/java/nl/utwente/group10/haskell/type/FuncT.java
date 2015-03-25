package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;

/**
 * Type of a Haskell function.
 */
public class FuncT extends ConstT {
    /**
     * @param arg The argument type that this function type accepts.
     * @param result The result type that this function type returns.
     */
    public FuncT(final Type arg, final Type result) {
        super("->", arg, result);
    }

    @Override
    public final String toHaskellType() {
        return "(" + Joiner.on(" -> ").join(this.getArgs()) + ")";
    }
}
