package nl.utwente.group10.haskell.type;

/**
 * A specific, non-variable Haskell type.
 */
public class ConstT extends Type {
    /**
     * @param name The Haskell name of this type.
     */
    public ConstT(final String name) {
        super(name);
    }

    @Override
    public final boolean compatibleWith(Type other) {
        return this.equals(other);
    }

    @Override
    public final String toHaskellType() {
        return this.getName();
    }

    @Override
    public final String toString() {
        return "ConstT{" +
                "name='" + this.getName() + "'" +
                "}";
    }

    @Override
    protected final ConstT clone() {
        return new ConstT(this.getName());
    }
}
