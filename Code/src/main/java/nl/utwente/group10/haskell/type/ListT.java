package nl.utwente.group10.haskell.type;

public class ListT extends Type {
    private final Type elementType;

    public ListT(Type elementType) {
        super("List");
        this.elementType = elementType;
    }

    @Override
    public final boolean compatibleWith(Type other) {
        return other instanceof ListT && this.elementType.compatibleWith(((ListT) other).elementType);
    }

    @Override
    public final String toHaskellType() {
        return "[" + this.elementType.toHaskellType() + "]";
    }

    @Override
    public final String toString() {
        return "ListT{" +
                "elementType=" + elementType +
                '}';
    }

    public final ListT clone() {
        return new ListT(this.elementType);
    }
}
