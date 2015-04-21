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

    /**
     * @param exprTypes Map of Expr types.
     * @param typeClasses Multimap of type classes.
     */
    public Env(Map<String, Type> exprTypes, HashMultimap<Type, TypeClass> typeClasses) {
        this.exprTypes = exprTypes;
        this.typeClasses = typeClasses;
    }

    public Env() {
        this(new HashMap<String, Type>(), HashMultimap.create());
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

    /**
     * @return A mapping between the name of a type class and its object.
     */
    public final Map<String, TypeClass> getTypeClasses() {
        Map<String, TypeClass> typeClasses = new HashMap<>();

        for (TypeClass tc : this.typeClasses.values()) {
            typeClasses.put(tc.getName(), tc);
        }

        return typeClasses;
    }

    /**
     * Joins two environments in a special way. Returns a new Env with the expression types from the first Env and the
     * type classes from the second Env.
     * @param exprEnv The first environment.
     * @param classEnv The second environment.
     * @return A new environment containing the expressions from the first Env and the type classes from the second Env.
     */
    public static Env join(Env exprEnv, Env classEnv) {
        return new Env(exprEnv.exprTypes, classEnv.typeClasses);
    }
}
