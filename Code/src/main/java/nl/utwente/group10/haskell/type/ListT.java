package nl.utwente.group10.haskell.type;

public class ListT extends Type {
    private Type elementType;

    public ListT(Type elementType) {
        this.elementType = elementType;
    }

    public String toString() {
        return "[" + elementType.toString() + "]";
    }
}
