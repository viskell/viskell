package nl.utwente.group10.haskell.type;

import java.util.IdentityHashMap;

import nl.utwente.group10.haskell.type.TypeVar.TypeInstance;

public class TypeCon extends ConcreteType implements Comparable<TypeCon>{
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
    protected TypeCon getFreshInstance(IdentityHashMap<TypeInstance, TypeVar> staleToFresh) {
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
