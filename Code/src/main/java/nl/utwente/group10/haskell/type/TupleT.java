package nl.utwente.group10.haskell.type;

import java.util.Arrays;
import java.util.Map;

/**
 * Tuple type. Has a fixed number of elements.
 */
public class TupleT extends CompositeType {
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
    public final TupleT getResolvedType(final Map<VarT, Type> types) {
        final Type[] elementTypes = new Type[this.elementTypes.length];

        for (int i = 0; i < this.elementTypes.length; i++) {
            if (this.elementTypes[i] instanceof CompositeType) {
                elementTypes[i] = ((CompositeType) this.elementTypes[i]).getResolvedType(types);
            } else if (this.elementTypes[i] instanceof VarT && types.containsKey(this.elementTypes[i])) {
                elementTypes[i] = types.get(this.elementTypes[i]);
            } else {
                elementTypes[i] = this.elementTypes[i];
            }
        }

        return new TupleT(elementTypes);
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
