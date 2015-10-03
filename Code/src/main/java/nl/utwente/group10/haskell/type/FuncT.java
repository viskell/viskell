package nl.utwente.group10.haskell.type;

import java.util.IdentityHashMap;

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
    public final String toHaskellType(final int fixity) {
        final StringBuilder out = new StringBuilder();
        final Type[] args = this.getArgs();

        out.append(args[0].toHaskellType(1));

        for (int i = 1; i < args.length; i++) {
        	out.append(" -> ");
        	final int fix = i == args.length-1 ? 0 : 1;
            out.append(args[i].toHaskellType(fix));
        }

        if (fixity > 0)
        	return "(" + out.toString() + ")";
        	
        return out.toString();
    }

    @Override
    protected FuncT getFreshInstance(IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh) {
        Type[] args = getFreshArgs(staleToFresh);
        return new FuncT(args[0], args[1]);
    }
}
