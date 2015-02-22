package nl.utwente.group10.haskell.type;

import com.google.common.base.Joiner;

import java.util.Arrays;

public class TupleT extends Type {
    private Type[] elementTypes;

    public TupleT(Type... elementTypes) {
        super("Tuple");
        this.elementTypes = elementTypes.clone();
    }

    @Override
    public final boolean compatibleWith(Type other) {
        return false; // TODO
    }

    @Override
    public final String toHaskellType() {
        return "(" + Joiner.on(", ").join(elementTypes) + ")";
    }

    @Override
    public final String toString() {
        return "TupleT{" +
                "elementTypes=" + Arrays.toString(elementTypes) +
                '}';
    }

    public TupleT clone() {
        return new TupleT(this.elementTypes);
    }
}
