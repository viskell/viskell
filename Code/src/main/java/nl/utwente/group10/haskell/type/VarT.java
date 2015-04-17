package nl.utwente.group10.haskell.type;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
     * The type constraints for this type.
     */
    private Set<Type> constraints;

    /**
     * @param name Identifier for this type. Identifiers are not used in the type checking progress, different
     *             {@code VarT} instances with the same name are not equal.
     * @param instance The instance of this type.
     * @param constraints The set of constraints for this type.
     */
    public VarT(final String name, final Type instance, final Set<Type> constraints) {
        this.name = name.toLowerCase();
        this.instance = Optional.ofNullable(instance);
        this.constraints = constraints;
    }

    /**
     * @param name Identifier for this type. Identifiers are not used in the type checking progress, different
     *             {@code VarT} instances with the same name are not equal.
     */
    public VarT(final String name) {
        this(name, null, new HashSet<Type>());
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
     * Checks whether the given type is within the constraints. If the set of constraints is empty, every type is within
     * the constraints.
     * @param type The type to check.
     * @return Whether the given type is within the constraints of this type.
     */
    public final boolean hasConstraint(Type type) {
        boolean out = true;

        if (!this.constraints.isEmpty()) {
            out = this.constraints.contains(type);
        }

        return out;
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

    /**
     * Builts a constraints set from a list of type classes.
     * @param typeclasses The type classes.
     * @return The constraints set.
     */
    public static Set<Type> buildConstraints(TypeClass ... typeclasses) {
        Set<Type> constraints = new HashSet<Type>();

        for (TypeClass typeclass : typeclasses) {
            constraints.addAll(typeclass.getTypes());
        }

        return constraints;
    }
}
