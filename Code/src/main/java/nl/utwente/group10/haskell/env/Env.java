package nl.utwente.group10.haskell.env;

import java.util.*;

import com.google.common.collect.HashMultimap;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;

/**
 * Haskell Environment.
 */
public class Env {
    /**
     * Map containing the types of expressions known to the environment.
     */
    private Map<String, Type> exprTypes;

    /**
     * Map containing the string representation of the types of the expressions known to the environment.
     */
    private Map<String, String> exprStringTypes;

    /**
     * Map containing the types for each type class.
     */
    private HashMultimap<Type, TypeClass> typeClasses;

    /**
     * @param exprTypes Map of Expr types.
     * @param exprStringTypes Map of Expr types in original string representation.
     * @param typeClasses Multimap of type classes.
     */
    public Env(Map<String, Type> exprTypes, Map<String, String> exprStringTypes, HashMultimap<Type, TypeClass> typeClasses) {
        this.exprTypes = exprTypes;
        this.exprStringTypes = exprStringTypes;
        this.typeClasses = typeClasses;
    }

    public Env(Map<String, Type> exprTypes, Map<String, String> exprStringTypes, Collection<TypeClass> typeClasses) {
        this(exprTypes, exprStringTypes, Env.buildTypeClasses(typeClasses));
    }

    public Env() {
        this(new HashMap<String, Type>(), new HashMap<String, String>(), HashMultimap.create());
    }

    /**
     * @return The map of known expression names and their types.
     */
    public final Map<String, Type> getExprTypes() {
        return this.exprTypes;
    }

    /**
     * @param name The name of the expression.
     * @return An Optional containing the Type for the given expression, if any.
     */
    public final Optional<Type> getFreshExprType(String name) {
        if (this.exprStringTypes.containsKey(name)) {
            TypeBuilder builder = new TypeBuilder(this.getTypeClasses());
            return Optional.ofNullable(builder.build(this.exprStringTypes.get(name)));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Adds an expression to this environment.
     * @param name The name of the expression.
     * @param signature The signature of the expression.
     * @return Expression to use in the future.
     */
    public final Ident addExpr(String name, String signature) {
        TypeBuilder builder = new TypeBuilder(this.getTypeClasses());
        Type type = builder.build(signature);
        this.exprStringTypes.put(name, signature);
        this.exprTypes.put(name, type);
        return new Ident(name);
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
     * Builds a Multimap from Type to TypeClass given a set of TypeClass objects.
     * @param typeClasses The TypeClass objects to include.
     * @return A Multimap containing the given TypeClass objects.
     */
    public static HashMultimap<Type, TypeClass> buildTypeClasses(final Collection<TypeClass> typeClasses) {
        HashMultimap<Type, TypeClass> result = HashMultimap.create();

        for (TypeClass typeClass : typeClasses) {
            for (Type type : typeClass.getTypes()) {
                result.put(type, typeClass);
            }
        }

        return result;
    }
}
