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
    public final static class TypeInstance {
        /**
         * The textual representation of the type variable.
         */
        private final String name;

        /**
         * A concrete instantiated type or null.
         */
        private ConcreteType type;

        /**
         * The type constraints for this instance.
         */
        private Set<TypeClass> constraints;

        /**
         * @param name The textual representation of the type variable.
         * @param type The concrete instance of this type, might be null.
         * @param constraints The set of constraints for this type.
         */
        private TypeInstance(String name, ConcreteType type, final Set<TypeClass> constraints) {
            this.name = name;
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
            if (this.type == null) {
                throw new NullPointerException("Getting invalid type instance");
            }

            return this.type;
        }

        /**
         * @throws IllegalStateException when is isPresent() is true
         * @param The new instance of this type.
         */
        private void set(ConcreteType type) {
            if (this.type != null) {
                throw new IllegalStateException("Type instance already set");
            }
            
            this.type = type;
        }

        /**
         * Share the constraints between both type instance, thus unifying the constraints.
         * @param the other type instance.
         */
        private void shareConstraints(TypeInstance other) {
            this.constraints = TypeClass.simplifyConstraints(Sets.union(this.constraints, other.constraints));
        }

        /**
         * @return The textual representation of the type variable.
         */
        private String getName() {
            return this.name;
        }
        
        /**
         * @param The fixity of the context the type is shown in.
         * @return The Haskell (type) representation of this type.
         */
        private final String toHaskellType(final int fixity) {
            if (this.type != null) {
                return this.type.toHaskellType(fixity);
            }

            final StringBuilder out = new StringBuilder();

            if (this.constraints.isEmpty()) {
                out.append(this.getName());
            } else if (fixity < 9 && this.constraints.size() == 1) {
                out.append(this.constraints.iterator().next().getName() + " " + this.getName());
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
        
    }

    /**
     * The reference to the potential concrete instance for this type.
     */
    private TypeInstance instance;

    /**
     * @param name The textual representation of the type variable.
     * Identifiers are not used in the type checking progress, 
     * different {@code TypeVar} instances with the same name are not equal.
     * @param constraints The set of constraints for this type.
     * @param instance The concrete instance of this type, might be null.
     */
    TypeVar(final String name, final Set<TypeClass> constraints, final ConcreteType type) {
        this.instance = new TypeInstance(name.toLowerCase(), type, constraints);
    }

    /**
     * @param name The textual representation of the type variable.
     * Identifiers are not used in the type checking progress,
     * different {@code TypeVar} instances with the same name are not equal.
     * @param constraints The set of constraints for this type
     */
    TypeVar(final String name, final TypeClass... constraints) {
        this(name, new HashSet<TypeClass>(Arrays.asList(constraints)), null);
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

    /*
    * @throws IllegalStateException when hasInstance() is true
    * @param The concrete type this type variable is unified with
    */
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

    /**
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
    public final boolean hasConstraint(TypeCon type) {
        return this.instance.constraints.stream().allMatch(tc -> tc.hasType(type));
    }

    /**
     * Extends the constraint set with extra type class, warning: the type variable may not been used in typechecking yet.   
     * @param typeClass to be added to this type variable
     */
    public void introduceConstraint(TypeClass typeClass) {
        this.instance.constraints.add(typeClass);
    }

    @Override
    public final String toHaskellType(final int fixity) {
        return this.instance.toHaskellType(fixity);
    }

    @Override
    public Type getFresh(TypeScope scope) {
        if (this.instance.isPresent()) {
            return this.instance.get().getFresh();
        }

        return scope.pickFreshTypeVar(this);
    }
    
    /**
     * This internal method should only be called from TypeScope
     * @param staleToFresh The mapping between known type instances and their related fresh type variables.
     * @return A refreshed type variable.
     */
    protected TypeVar pickFreshTypeVarInstance(IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh) {
        if (staleToFresh.containsKey(this.instance)) {
            return staleToFresh.get(this.instance);
        }

        TypeVar fresh = new TypeVar(this.instance.name, new HashSet<>(this.instance.constraints), null);
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
        String tmp = String.format("%s(%s)", this.getName(), Integer.toHexString(this.instance.hashCode()));
        return this.instance.isPresent() ? tmp + ":" + this.instance.get().toString() : tmp;
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
