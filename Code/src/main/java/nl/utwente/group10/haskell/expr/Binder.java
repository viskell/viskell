package nl.utwente.group10.haskell.expr;

import java.util.IdentityHashMap;

import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeChecker;
import nl.utwente.group10.haskell.type.TypeVar;

/**
 * A Binder is the definition side of a local variable, it is used link variable to their binding constructs, such as lambdas 
 */
public final class Binder {

    /** The name of this binder */
    private final String name;
    
    /** An internal type used in the type inference process */
    private TypeVar inferenceType;
    
    /** An optional */
    private final Type annotation;
    
    /** 
     * @param name of this Binder
     */
    public Binder(String name) {
        this.name = name;
        this.annotation = null;
    }

    /**
     * @param name of this binder
     * @param annotation a type to restrict the potential types of this binder
     */
    public Binder(String name, Type annotation){
        this.name = name;
        this.annotation = annotation;
    }

    /**
     * @return The name (made unique) of this binder, for avoiding name conflicts in code generation
     */
    public final String getUniqueName() {
        return name + "__" + Integer.toHexString(this.hashCode());
    }

    /**
     * @return the type annotation, might be null
     */
    public final Type getAnnotation() {
        return this.annotation;
    }

    protected Type refreshBinderType(IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh) throws HaskellTypeError {
        this.inferenceType = Type.var(this.name);
        if (this.annotation != null) {
            TypeChecker.unify(this.annotation.getFreshInstance(staleToFresh), this.inferenceType);
        } 
        
        return this.inferenceType;
    }

    /**
     * @return the type for the use site of this binder 
     * @throws HaskellTypeError if this function is called before refreshBinderType
     */
    public final Type getBoundType() throws HaskellTypeError {
        if (this.inferenceType == null) {
            // technically it is an error in scoping but this will do for now
            throw new HaskellTypeError("Using the type before it is bound, of binder: " + this.name);
        }
        
        return this.inferenceType;
    }
    
    @Override
    public final String toString() {
        return this.getUniqueName();
    }
    
}
