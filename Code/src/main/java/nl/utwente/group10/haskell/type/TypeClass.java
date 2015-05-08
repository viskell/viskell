package nl.utwente.group10.haskell.type;

import nl.utwente.group10.haskell.HaskellObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Haskell TypeClass.
 */
public class TypeClass extends HaskellObject {
    /**
     * The name of this type class.
     */
    private String name;

    /**
     * The types that are a member of this type class.
     */
    private Set<Type> types;

    /**
     * @param name The name of this type class.
     * @param types The types that are a member of this type class.
     */
    public TypeClass(String name, ConstT ... types) {
        this.name = name;
        this.types = new HashSet<>(Arrays.asList(types));
    }

    /**
     * @return The name of this type class.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @return The types in this type class.
     */
    public final Set<Type> getTypes() {
        return this.types;
    }

    /**
     * @param type The type to check.
     * @return Whether the given type is in this type class.
     */
    public final boolean hasType(Type type) {
        return this.types.stream().anyMatch(t -> t.compareTo(type) == 0);
    }

    public final String toString() {
        return String.format("%s%s", this.name, this.types.toString());
    }
}
