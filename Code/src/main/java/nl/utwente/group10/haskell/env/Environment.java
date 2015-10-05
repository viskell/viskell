package nl.utwente.group10.haskell.env;

import java.util.*;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;
import nl.utwente.group10.haskell.typeparser.TypeBuilder;

/**
 * Haskell Environment.
 */
public class Environment {
    /**
     * Map containing the types of function available in the environment.
     */
    private Map<String, FunctionInfo> functions;

    /**
     * Map containing the type classes by name.
     */
    private HashMap<String, TypeClass> typeClasses;

    /**
     * @param functions Map of available functions.
     * @param typeClasses Map of known type classes.
     */
    public Environment(Map<String, FunctionInfo> functions, HashMap<String, TypeClass> typeClasses) {
        this.functions = functions;
        this.typeClasses = typeClasses;
    }

    public Environment() {
        this(new HashMap<>(), new HashMap<>());
    }

    /**
     * @param name The name of the function.
     * @return The fresh Type for the given function, or null if it doesn't exist.
     */
    public final Type getFreshExprType(String name) {
        if (this.functions.containsKey(name)) {
            return this.functions.get(name).getFreshSignature();
        } else {
            return null;
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
     * Adds an type signature for function (lacking implementation) to this environment.
     * @param name The name of the expression.
     * @param signature The signature of the expression.
     */
    public final void addTestSignature(String name, String signature) {
        TypeBuilder builder = new TypeBuilder(this.typeClasses);
        Type type = builder.build(signature);
        this.functions.put(name, new CatalogFunction(name, "!TEST!", type, ""));
    }

    /**
     * Adds the given type class to the environment.
     * @param typeclass The type class to add.
     */
    public final void addTypeClass(TypeClass typeclass) {
        this.typeClasses.put(typeclass.getName(), typeclass);
    }

}
