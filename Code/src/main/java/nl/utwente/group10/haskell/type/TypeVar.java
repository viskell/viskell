package nl.utwente.group10.haskell.type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import com.google.common.collect.Sets;

/**
 * Variable type.
 */
public class TypeVar extends Type {

    /**
     * An optional mutable reference to a concrete type.
     */
    final static class TypeInstance {
        /**
         * Base name of the type variable.
         */
        private final String prefix;

        /**
         * Number to make a name of the type variable unique.
         */
        private final int uid;

        /**
         * A concrete instantiated type or null.
         */
        private ConcreteType type;

        /**
         * The type constraints for this instance.
         */
        private Set<TypeClass> constraints;

        /**
         * @param The instance of this type.
         * @param constraints The set of constraints for this type.
         */
        private TypeInstance(String prefix, int uid, ConcreteType type, final Set<TypeClass> constraints) {
            this.prefix = prefix;
            this.uid = uid;
            this.type = type;
            this.constraints = constraints;
        }

        /**
         * @return Whether there is a concrete type.
         */
        private boolean isPresent() {
            return this.type != null;
        }

        /**
         * @throws NullPointerException when isPresent() is false
         * @return The concrete type instance
         */
        private ConcreteType get() {
            if (this.type == null)
                throw new NullPointerException("Getting invalid type instance");

            return this.type;
        }

        /**
         * @param The new instance of this type.
         */
        private void set(ConcreteType type) {
            this.type = type;
        }

        /**
         * Share the constraints between both type instance, thus unifying the constraints.
         * 
         * @param the other type instance.
         */
        private void shareConstraints(TypeInstance other) {
            this.constraints = Sets.union(this.constraints, other.constraints);
        }

        /**
         * @return The name of this variable type.
         */
        private String getName() {
            if (this.uid == 0) {
                return this.prefix;
            }

            return this.prefix + Integer.toString(this.uid);
        }
    }

    /**
     * The reference to the potential concrete instance for this type.
     */
    private TypeInstance instance;

    /**
     * @param name Identifier for this type.
     * Identifiers are not used in the type checking progress, 
     * different {@code TypeVar} instances with the same name are not equal.
     * @param constraints The set of constraints for this type.
     * @param instance The instance of this type.
     */
    public TypeVar(final String prefix, final int uid, final Set<TypeClass> constraints, final ConcreteType type) {
        this.instance = new TypeInstance(prefix.toLowerCase(), uid, type, constraints);
    }

    /**
     * @param name Identifier for this type.
     * Identifiers are not used in the type checking progress,
     * different {@code TypeVar} instances with the same name are not equal.
     * @param typeclasses The type classes that are accepted by this type.
     */
    public TypeVar(final String name, final TypeClass... typeclasses) {
        this(name, 0, new HashSet<TypeClass>(Arrays.asList(typeclasses)), null);
    }

    /**
     * @return The name of this variable type.
     */
    public final String getName() {
        return this.instance.getName();
    }

    /**
     * @return Whether this type variable has been instantiated with a concrete type.
     */
    public final boolean hasInstance() {
        return this.instance.isPresent();
    }

    /**
     * @throws NullPointerException when hasInstance() is false
     * @return The concrete type this type variable has been instantiated with.
     */
    public final ConcreteType getInstantiatedType() {
        return this.instance.get();
    }

    public final void setConcreteInstance(ConcreteType type) {
        this.instance.set(type);
    }

    /**
     * Use the same type instance for both type variable, effectively unifying them.
     * 
     * @param the other type variable.
     */
    public final void shareInstanceOf(TypeVar other) {
        other.instance.shareConstraints(this.instance);
        this.instance = other.instance;
    }

    /*
     * @return Whether this type variable is constrained.
     */
    public final boolean hasConstraints() {
        return !this.instance.constraints.isEmpty();
    }

    /**
     * Checks whether the given type is within the constraints. 
     * If the set of constraints is empty, every type is within the constraints.
     * 
     * @param type The type to check.
     * @return Whether the given type is within the constraints of this type.
     */
    public final boolean hasConstraint(ConstT type) {
        return this.instance.constraints.stream().allMatch(tc -> tc.hasType(type));
    }

    @Override
    public final String toHaskellType(final int fixity) {
        if (this.instance.isPresent()) {
            return this.instance.get().toHaskellType(fixity);
        }

        final StringBuilder out = new StringBuilder();

        if (this.instance.constraints.isEmpty()) {
            out.append(this.getName());
        } else {
            out.append("(");

            int i = 0;

            for (TypeClass tc : this.instance.constraints) {
                out.append(String.format("%s %s", tc.getName(), this.getName()));
                if (i + 1 < this.instance.constraints.size()) {
                    out.append(", ");
                }
                i++;
            }

            out.append(")");
        }

        return out.toString();
    }

    @Override
    protected Type getFreshInstance(IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh) {
        if (this.instance.isPresent()) {
            return this.instance.get().getFresh();
        }

        if (staleToFresh.containsKey(this.instance)) {
            return staleToFresh.get(this.instance);
        }

        TypeVar fresh = new TypeVar(this.instance.prefix, this.instance.uid,
                new HashSet<TypeClass>(this.instance.constraints), null);
        staleToFresh.put(this.instance, fresh);
        return fresh;
    }

    @Override
    public boolean containsOccurenceOf(TypeVar tvar) {
        // If type variable share the same instance then they have been unified to a single one.
        if (this.instance == tvar.instance) {
            return true;
        }

        if (!this.instance.isPresent()) {
            return false;
        }

        return this.instance.get().containsOccurenceOf(tvar);
    }

    @Override
    public final String toString() {
        return this.instance.isPresent() ? String.format("%s:%s", this.getName(), this.instance.get()) : this.getName();
    }

    /**
     * This method simply checks whether the reference of instance in the type variables are the same.
     * The uniqueness of type variable depends on this because the meaning of the name is context sensitive.
     * This is why we leave the task of using the right type variable to the programmer that implements this type system
     * - there would be no clear and easy way of doing this automagically.
     *
     * @param obj The object to compare with.
     * @return Whether the given object is equal to this object.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeVar)) {
            return false;
        }

        TypeVar other = (TypeVar) obj;
        return this.instance == other.instance;
    }

}
