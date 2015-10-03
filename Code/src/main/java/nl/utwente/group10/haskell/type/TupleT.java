package nl.utwente.group10.haskell.type;

import java.util.IdentityHashMap;

/**
 * Tuple type.
 */
public class TupleT extends ConstT {
    /**
     * @param args The types of the elements for this tuple.
     */
    public TupleT(Type... args) {
        super("(,)", args);
    }

    @Override
    public final String toHaskellType(final int fixity) {
        StringBuilder out = new StringBuilder();
        Type[] args = this.getArgs();

        out.append("(");

        for (int i = 0; i < args.length; i++) {
            out.append(args[i].toHaskellType(0));

            if (i + 1 < args.length) {
                out.append(", ");
            }
        }

        out.append(")");
        return out.toString();
    }

    @Override
    protected TupleT getFreshInstance(IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh) {
        return new TupleT(this.getFreshArgs(staleToFresh));
    }
}
