package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;

public class TupleT extends Type {
    private Type[] elementTypes;

    public TupleT(Type... elementTypes) {
        this.elementTypes = elementTypes;
    }

    public String toString() {
        return "(" + Joiner.on(", ").join(elementTypes) + ")";
    }
}
