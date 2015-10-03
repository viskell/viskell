package nl.utwente.group10.haskell.type;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Variable type.
 */
public class VarT extends Type {
    /**
     * Base name of the type variable.
     */
    private final String prefix;

    /**
     * Number to make a name of the type variable unique.
     */
    private final int uid;

    /**
     * The instance for this type.
     */
    private Optional<Type> instance;

    /**
     * The type constraints for this type.
     */
    private Set<TypeClass> constraints;

    /**
     * @param name Identifier for this type. Identifiers are not used in the type checking progress, different
     *             {@code VarT} instances with the same name are not equal.
     * @param constraints The set of constraints for this type.
     * @param instance The instance of this type.
     */
    public VarT(final String prefix, final int uid, final Set<TypeClass> constraints, final Type instance) {
        this.prefix = prefix.toLowerCase();
        this.uid = uid;
        this.instance = Optional.ofNullable(instance);
        this.constraints = constraints;
    }

    /**
     * @param name Identifier for this type. Identifiers are not used in the type checking progress, different
     *             {@code VarT} instances with the same name are not equal.
     * @param typeclasses The type classes that are accepted by this type.
     */
    public VarT(final String name, final TypeClass ... typeclasses) {
        this(name, 0, new HashSet<TypeClass>(Arrays.asList(typeclasses)), null);
    }

    /**
     * @return The name of this variable type.
     */
    public final String getPrefix() {
        return this.prefix;
    }

    /**
     * @return The name of this variable type.
     */
    public final String getName() {
        if (this.uid == 0) {
            return this.prefix;
        }

        return this.prefix + Integer.toString(this.uid);
    }

    /**
     * @return The {@code Optional} that may contain an instance for this variable type.
     */
    public final Optional<Type> getInstance() {
        return this.instance;
    }

    /**
     * @return Whether this type has any constraints.
     */
    public final boolean hasConstraints() {
        return !this.constraints.isEmpty();
    }

    /**
     * Checks whether the given type is within the constraints. If the set of constraints is empty, every type is within
     * the constraints.
     * @param type The type to check.
     * @return Whether the given type is within the constraints of this type.
     */
    public final boolean hasConstraint(Type type) {
        return constraints.stream().allMatch(tc -> tc.hasType(type));
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
    public final String toHaskellType(final int fixity) {
        final StringBuilder out = new StringBuilder();

        if (this.constraints.isEmpty()) {
            out.append(this.getName());
        } else {
            out.append("(");

            int i = 0;

            for (TypeClass tc : this.constraints) {
                out.append(String.format("%s %s", tc.getName(), this.getName()));
                if (i + 1 < this.constraints.size()) {
                    out.append(", ");
                }
                i++;
            }

            out.append(")");
        }

        return out.toString();
    }

    @Override
    public VarT getFresh() {
        Type instance = null;

        if (this.instance.isPresent()) {
            instance = this.instance.get().getFresh();
        }

        return new VarT(this.prefix, this.uid, this.constraints, instance);
    }

    @Override
    public final String toString() {
        return this.instance.isPresent() ? String.format("%s:%s", this.getName(), this.instance.get()) : this.getName();
    }

    /** Return the set of typeclasses that is the union of both arguments' constraints. */
    public static Set<TypeClass> union(VarT a, VarT b) {
        return Sets.union(a.constraints, b.constraints);
    }

    /**
     * This method simply checks whether the given objects are the same (i.e. point to the same bit of memory). The
     * uniqueness of VarTs depends on this because the meaning of the name is context sensitive. This is why we leave
     * the task of using the right VarTs to the programmer that implements this type system - there would be no clear
     * and easy way of doing this automagically.
     *
     * @param obj The object to compare with.
     * @return Whether the given object is equal to this object.
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int compareTo(final Type type) {
        return this.equals(type) ? 0 : -1;
    }
}
