package nl.utwente.group10.haskell.type;

/**
 * List type.
 */
public class ListT extends Type {
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
