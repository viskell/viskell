package nl.utwente.group10.haskell.env;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;

/**
 * Haskell Environment.
 */
public class Env {
    /**
     * Map containing the types of expressions known to the environment.
     */
    private Map<String, Type> exprTypes;

    /**
     * Map containing the types for each type class.
     */
    private HashMultimap<Type, TypeClass> typeClasses;

    public Env() {
        this.exprTypes = new HashMap<String, Type>();
        this.typeClasses = HashMultimap.create();
    }

    /**
     * @return The map of known expression names and their types.
     */
    public final Map<String, Type> getExprTypes() {
        return this.exprTypes;
    }

    /**
     * Adds the given type class to the environment.
     * @param typeclass The type class to add.
     */
    public final void addTypeClass(TypeClass typeclass) {
        for (Type type : typeclass.getTypes()) {
            this.typeClasses.put(type, typeclass);
        }
    }

    /**
     * @param type The type to get the type classes for.
     * @return The list of type classes for the given type.
     */
    public final Set<TypeClass> getTypeClasses(Type type) {
        return this.typeClasses.get(type);
    }
}
