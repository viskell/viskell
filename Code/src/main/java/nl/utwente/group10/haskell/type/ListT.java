package nl.utwente.group10.haskell.type;

import java.util.Map;

/**
 * List type.
 */
public class ListT extends CompositeType {
    /**
     * Type of the elements in the list.
     */
    private final Type elementType;

    /**
     * @param elementType The type of the elements in the list.
     */
    public ListT(final Type elementType) {
        this.elementType = elementType;
    }

    @Override
    public final boolean compatibleWith(final Type other) {
        return other instanceof ListT && this.elementType.compatibleWith(((ListT) other).elementType);
    }

    @Override
    public final ListT getResolvedType(final Map<VarT, Type> types) {
        final Type elementType;

        if (this.elementType instanceof CompositeType) {
            elementType = ((CompositeType) this.elementType).getResolvedType(types);
        } else if (this.elementType instanceof VarT && types.containsKey(this.elementType)) {
            elementType = types.get(this.elementType);
        } else {
            elementType = this.elementType;
        }

        return new ListT(elementType);
    }

    @Override
    public final String toHaskellType() {
        return "[" + this.elementType.toHaskellType() + "]";
    }

    @Override
    public final String toString() {
        return "ListT{" +
                "elementType=" + this.elementType +
                '}';
    }
}
