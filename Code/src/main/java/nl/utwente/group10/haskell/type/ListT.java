package nl.utwente.group10.haskell.type;

import java.util.IdentityHashMap;

/**
 * List type.
 */
public class ListT extends ConstT {
    /**
     * @param arg The type of elements that this list can contain.
     */
    public ListT(final Type arg) {
        super("[]", arg);
    }

    @Override
    public final String toHaskellType(final int fixity) {
        return "[" + this.getArgs()[0].toHaskellType(0) + "]";
    }

    @Override
    public ListT getFreshInstance(IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh) {
        Type[] freshArgs = this.getFreshArgs(staleToFresh);
        return new ListT(freshArgs[0]);
    }
}
