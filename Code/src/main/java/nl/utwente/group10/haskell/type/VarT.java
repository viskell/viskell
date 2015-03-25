package nl.utwente.group10.haskell.type;

import java.util.Optional;

/**
 * Variable type.
 */
public class VarT extends Type {
    /**
     * Identifier for type checking within {@code FuncT} and {@code TupleT} types.
     */
    private final String name;

    /**
     * The instance for this type.
     */
    private Optional<Type> instance;

    /**
     * @param name Identifier for this type. Identifiers are not used in the type checking progress, different
     *             {@code VarT} instances with the same name are not equal.
     * @param instance The instance of this type.
     */
    public VarT(final String name, final Type instance) {
        this.name = name.toLowerCase();
        this.instance = Optional.ofNullable(instance);
    }

    /**
     * @param name Identifier for this type. Identifiers are not used in the type checking progress, different
     *             {@code VarT} instances with the same name are not equal.
     */
    public VarT(final String name) {
        this(name, null);
    }

    /**
     * @return The name of this variable type.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @return The {@code Optional} that may contain an instance for this variable type.
     */
    public final Optional<Type> getInstance() {
        return this.instance;
    }

    /**
     * @param instance The new instance to set for this variable type.
     */
    public final void setInstance(final Type instance) {
        this.instance = Optional.ofNullable(instance);
    }

    @Override
    public final Type prune() {
        final Type pruned;

        if (this.getInstance().isPresent()) {
            pruned = this.getInstance().get().prune();
            this.setInstance(pruned);
        } else {
            pruned = this;
        }

        return pruned;
    }

    @Override
    public final String toHaskellType() {
        return this.name;
    }

    @Override
    public final String toString() {
        return this.instance.isPresent() ? String.format("%s:%s", this.name, this.instance.get()) : this.name;
    }
}
