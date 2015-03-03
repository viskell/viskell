package nl.utwente.group10.haskell.type;

/**
 * A specific, non-variable Haskell type.
 */
public class ConstT extends Type {
    /**
     * The Haskell name of this type.
     */
    private final String name;

    /**
     * @param name The Haskell name of this type.
     */
    public ConstT(final String name) {
        this.name = name;
    }

    @Override
    public final boolean compatibleWith(final Type other) {
        return this.equals(other);
    }

    @Override
    public final String toHaskellType() {
        return this.name;
    }

    @Override
    public final String toString() {
        return "ConstT{" +
                "name='" + this.name + "'" +
                "}";
    }
}
