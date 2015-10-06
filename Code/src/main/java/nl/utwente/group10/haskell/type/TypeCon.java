package nl.utwente.group10.haskell.type;

public class TypeCon extends ConcreteType implements Comparable<TypeCon> {
    /**
     * The name of type constructor.
     */
    protected final String name;

    TypeCon(String name) {
        this.name = name;
    }

    /**
     * @return the name of the type constructor
     */
    public String getName() {
        return name;
    }

    @Override
    public String toHaskellType(int fixity) {
        return this.name;
    }

    @Override
    public TypeCon getFresh(TypeScope scope) {
        return this;
    }

    @Override
    public boolean containsOccurenceOf(TypeVar tvar) {
        return false;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(TypeCon other) {
        return this.name.compareTo(other.name);
    }

}
