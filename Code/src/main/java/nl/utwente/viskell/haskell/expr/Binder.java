package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;

/**
 * A Binder is the definition side of a local variable, it is used link variable to their binding constructs, such as lambdas 
 */
public class Binder {

    /** The name of this binder */
    private final String name;
    
    /** An internal type used in the type inference process */
    private Type inferenceType;
    
    /** An optional type annotation to restrict the type of this binder */
    private Type annotation;
    
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
    public String getUniqueName() {
        return name + "__" + Integer.toHexString(this.hashCode());
    }

    /**
     * @return The base name of this binder
     */
    public String getBaseName() {
        return this.name;
    }
    
    /**
     * Refreshes the internal type of the binder for type inference
     * @param scope wherein the fresh type is constructed
     */
    public Type refreshBinderType(final TypeScope scope) {
        if (this.annotation != null) {
            this.inferenceType = this.annotation.getFresh(scope);
        } else {
            this.inferenceType = scope.getVar(this.name);
        }
        return this.inferenceType;
    }

    /**
     * @return the type for the use site of this binder 
     * @throws RuntimeException if this function is called before refreshBinderType
     */
    public final Type getBoundType() {
        if (this.inferenceType == null) {
            // technically it is an error in scoping but this will do for now
            throw new RuntimeException("Using the type before it is bound, of binder: " + this.name);
        }
        
        return this.inferenceType;
    }
    
    /**
     * Sets the type annotation of the binder and makes its internal type fresh.
     * @param type to annotate this binder with.
     * @param scope wherein the fresh type is constructed.
     */
    public void setFreshAnnotation(Type type, TypeScope scope) {
        this.annotation = type;
        this.inferenceType = type.getFresh(scope);
    }

    /**
     * Replaces the type annotation and internal type with another type. 
     * @param type to annotate this binder with.
     */
    public void setAnnotationAsType(Type type) {
        this.annotation = type;
        this.inferenceType = type;
    }

    /** @return a fresh copy of the annotated type constraint, or null if absent. */
    public Type getFreshAnnotationType() {
        if (this.annotation != null) {
            return this.annotation.getFresh();
        }
        
        return null;
    }
    
    @Override
    public final String toString() {
        if (this.inferenceType == null) {
            return this.name;
        }
        
        return this.name + "::" + this.inferenceType.toString();
    }

}
