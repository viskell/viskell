package nl.utwente.group10.haskell.env;

import java.util.*;

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
     * Map containing the type classes by name.
     */
    private HashMap<String, TypeClass> typeClasses;

    /**
     * @param exprTypes Map of Expr types.
     * @param typeClasses Map of type classes.
     */
    public Env(Map<String, Type> exprTypes, HashMap<String, TypeClass> typeClasses) {
        this.exprTypes = exprTypes;
        this.typeClasses = typeClasses;
    }

    public Env() {
        this(new HashMap<String, Type>(), new HashMap<String, TypeClass>());
    }

    /**
     * @param name The name of the expression.
     * @return An Optional containing the Type for the given expression, if any.
     */
    public final Optional<Type> getFreshExprType(String name) {
        if (this.exprTypes.containsKey(name)) {
            return Optional.ofNullable(this.exprTypes.get(name).getFresh());
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * @param type The String representation of the type signature
     * @return Type built using the context of this environment
     */
    public final Type buildType(String type) {
        return new TypeBuilder(this.typeClasses).build(type);
    }

    /**
     * 
     */
    public final TypeClass lookupClass(String name) {
        return this.typeClasses.get(name);
    }
    
    /**
     * Adds an expression to this environment.
     * @param name The name of the expression.
     * @param signature The signature of the expression.
     */
    public final void addExpr(String name, String signature) {
        TypeBuilder builder = new TypeBuilder(this.typeClasses);
        Type type = builder.build(signature);
        this.exprTypes.put(name, type);
    }

    /**
     * Adds the given type class to the environment.
     * @param typeclass The type class to add.
     */
    public final void addTypeClass(TypeClass typeclass) {
        this.typeClasses.put(typeclass.getName(), typeclass);
    }

}
