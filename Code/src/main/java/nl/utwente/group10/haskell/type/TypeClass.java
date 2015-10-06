package nl.utwente.group10.haskell.type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Haskell TypeClass.
 */
public class TypeClass {
    /**
     * The name of this type class.
     */
    private String name;

    /**
     * The constructors that are a member of this type class.
     */
    private Set<TypeCon> cons;

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
        this.cons = new HashSet<>(Arrays.asList(cons));
        this.supers = new HashSet<>();
    }

    /**
     * @return The name of this type class.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @param tc The type constructor to add to this class 
     */
    public final void addType(TypeCon tc) {
        this.cons.add(tc);
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
     * @param type The type to check.
     * @return Whether the given type is in this type class.
     */
    public final boolean hasType(TypeCon type) {
        return this.cons.stream().anyMatch(t -> t.compareTo(type) == 0);
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
        return String.format("%s=>%s:%s", this.supers.stream().map(t ->t.getName()), this.name, this.cons.toString());
    }

}
