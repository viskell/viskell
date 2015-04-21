package nl.utwente.group10.haskell.catalog;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** A class entry in the Haskell catalog. */
public class ClassEntry implements Comparable<ClassEntry> {
    /** The name of this entry. */
    private String name;

    /** The instances of this class entry. */
    private Set<String> instances;

    /**
     * @param name The name of this entry.
     * @param instances The instances of this entry.
     */
    public ClassEntry(final String name, final Set<String> instances) {
        this.name = name;
        this.instances = instances;
    }

    /**
     * @return The name of the type class of this entry.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @return The names of the instances of the type class of this entry.
     */
    public final Set<String> getInstances() {
        return this.instances;
    }

    /**
     * Parses and returns the TypeClass for this Entry.
     * @param typeClasses The available type classes.
     * @return The TypeClass for this Entry.
     */
    public final TypeClass getTypeClass(Map<String, TypeClass> typeClasses) {
        TypeBuilder builder = new TypeBuilder(typeClasses);
        TypeClass tc = new TypeClass(this.getName());

        for (String instance : this.instances) {
            tc.getTypes().add(builder.build(instance));
        }

        return tc;
    }

    /**
     * Parses and returns the TypeClass for this Entry.
     * @return The TypeClass for this Entry.
     */
    public final TypeClass getTypeClass() {
        return this.getTypeClass(null);
    }

    @Override
    public int compareTo(ClassEntry entry) {
        return this.getName().compareTo(entry.getName());
    }
}
