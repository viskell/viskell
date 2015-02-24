package nl.utwente.group10.haskell.type;

import java.util.Arrays;

/**
 * Tuple type. Has a fixed number of elements.
 */
public class TupleT extends Type {
    /**
     * Types of the elements in the tuple.
     */
    private final Type[] elementTypes;

    /**
     * @param elementTypes Types of the elements in the tuple.
     */
    public TupleT(final Type... elementTypes) {
        this.elementTypes = elementTypes.clone();
    }

    @Override
    public final boolean compatibleWith(final Type other) {
        boolean compatible = true;

        if (other instanceof TupleT && this.elementTypes.length == ((TupleT) other).elementTypes.length) {
            for (int i = 0; i < this.elementTypes.length; i++) {
                if (!this.elementTypes[i].compatibleWith(((TupleT) other).elementTypes[i])) {
                    compatible = false;
                    break;
                }
            }
        } else {
            compatible = false;
        }

        return compatible;
    }

    @Override
    public final String toHaskellType() {
        final StringBuilder out = new StringBuilder();
        out.append("(");

        for (int i = 0; i < this.elementTypes.length; i++) {
            out.append(this.elementTypes[i].toHaskellType());

            if (i + 1 < this.elementTypes.length) {
                out.append(", ");
            }
        }

        out.append(")");
        return out.toString();
    }

    @Override
    public final String toString() {
        return "TupleT{" +
                "elementTypes=" + Arrays.toString(this.elementTypes) +
                '}';
    }
}
