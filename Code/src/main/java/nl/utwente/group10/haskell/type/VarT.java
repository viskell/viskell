package nl.utwente.group10.haskell.type;

import java.util.Arrays;
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
    private Set<TypeClass> constraints;

    /**
     * @param name Identifier for this type. Identifiers are not used in the type checking progress, different
     *             {@code VarT} instances with the same name are not equal.
     * @param constraints The set of constraints for this type.
     * @param instance The instance of this type.
     */
    public VarT(final String name, final Set<TypeClass> constraints, final Type instance) {
        this.name = name.toLowerCase();
        this.instance = Optional.ofNullable(instance);
        this.constraints = constraints;
    }

    /**
     * @param name Identifier for this type. Identifiers are not used in the type checking progress, different
     *             {@code VarT} instances with the same name are not equal.
     * @param typeclasses The type classes that are accepted by this type.
     */
    public VarT(final String name, final TypeClass ... typeclasses) {
        this(name, new HashSet<TypeClass>(Arrays.asList(typeclasses)), null);
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
        boolean out = false;

        if (!this.constraints.isEmpty()) {
            for (TypeClass typeclass : this.constraints) {
                if (typeclass.hasType(type)) {
                    out = true;
                }
            }
        } else {
            out = true;
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
        final StringBuilder out = new StringBuilder();

        if (this.constraints.isEmpty()) {
            out.append(this.name);
        } else {
            out.append("(");

            int i = 0;

            for (TypeClass tc : this.constraints) {
                out.append(String.format("%s %s", tc.getName(), this.name));
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

        return new VarT(this.name, this.constraints, instance);
    }

    @Override
    public final String toString() {
        return this.instance.isPresent() ? String.format("%s:%s", this.name, this.instance.get()) : this.name;
    }

    /**
     * Calculates the intersection of two VarT constraints and returns the set of matching type classes.
     * @param a The first type.
     * @param b The second type.
     * @return The set of type classes that are in both types.
     */
    public static Set<TypeClass> intersect(VarT a, VarT b) {
        //TODO: TEMPORARY WORKAROUND
        if (!a.hasConstraints()) return b.constraints;
        if (!b.hasConstraints()) return a.constraints;
        
        final Set<TypeClass> intersection = new HashSet<TypeClass>();

        for (TypeClass tc : a.constraints) {
            if (b.constraints.contains(tc)) {
                intersection.add(tc);
            }
        }

        return intersection;
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
