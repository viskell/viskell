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
        final StringBuilder out = new StringBuilder();
        final Type[] args = this.getArgs();

        out.append("(");

        for (int i = 0; i < args.length; i++) {
            out.append(args[i].toHaskellType());

            if (i + 1 < args.length) {
                out.append(" -> ");
            }
        }

        out.append(")");
        return out.toString();
    }
}
