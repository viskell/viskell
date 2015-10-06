package nl.utwente.group10.haskell.type;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Haskell TypeClass with its instances and superclasses.
 */
public class TypeClass {
    
    /**
     * An instance declaration 
     */
    final static class Instance implements Comparable<Instance> {

        /**
         * The type constructor that identifies this instance.
         */
        private final TypeCon typecon;
        
        /**
         * The number of type arguments that needs to be constrained to make this instance valid
         * For Example: Eq a => Eq (Maybe a) is the instance Maybe#1, and (Show l, Show r) => Show (Either l r) is Either#2
         * Most Prelude instances are simple enough to not required sub-constraints, so this number will be often just 0.  
         */
        private final int constrainedArgs;

        public Instance(TypeCon typecon, int constrainedArgs) {
            super();
            this.typecon = typecon;
            this.constrainedArgs = constrainedArgs;
        }

        @Override
        public String toString() {
            return this.typecon.toString() + "#" + this.constrainedArgs;
        }

        @Override
        public int compareTo(Instance other) {
            return this.typecon.getName().compareTo(other.typecon.getName());
        }

    }
    /**
     * The name of this type class.
     */
    private String name;

    /**
     * The instances of this type class.
     */
    private Set<Instance> instances;

    /**
     * The superclasses of this type class.
     */
    private Set<TypeClass> supers;
    
    /**
     * @param name The name of this type class.
     * @param types The types that are a member of this type class.
     */
    public TypeClass(String name, TypeCon ... cons) {
        this.name = name;
        this.instances = new HashSet<>();
        this.supers = new HashSet<>();
        for (TypeCon tc : cons) {
            this.addInstance(tc, 0);
        }
    }

    /**
     * @return The name of this type class.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @param tc The type constructor to add to this class
     * @param constrainedArgs the number of type parameter that needs to be constrained to make the instance valid
     */
    public final void addInstance(TypeCon tc, int constrainedArgs) {
            this.instances.add(new Instance(tc, constrainedArgs));
    }

    /**
     * @param tc The super class that this class requires 
     */
    public final void addSuperClass(TypeClass tc) {
        this.supers.add(tc);
        // Also transitively add all the superclasses of this superclass for easier simplification
        this.supers.addAll(tc.supers);
    }
    
    /**
     * @param type The type constructor to check.
     * @return Whether the given type constructor is in this type class.
     */
    public final boolean hasType(TypeCon type) {
        Instance inst = new Instance(type, 0);
        return this.instances.stream().anyMatch(i -> i.compareTo(inst) == 0);
    }

    /**
     * @param classes A set of class constraints to be simplified
     * @return A set of class constraints with all implied super classes removed
     */
    public static Set<TypeClass> simplifyConstraints(Set<TypeClass> classes) {
        if (classes.size() <= 1) {
            return classes;
        }
        
        Set<TypeClass> allSupers = new HashSet<>();
        for (TypeClass tc : classes) {
            allSupers.addAll(tc.supers);
        }

        return new HashSet<>(Sets.difference(classes, allSupers));
    }
    
    public final String toString() {
        return String.format("%s=>%s:%s", this.supers.stream().map(t ->t.getName()), this.name, this.instances.toString());
    }

}
