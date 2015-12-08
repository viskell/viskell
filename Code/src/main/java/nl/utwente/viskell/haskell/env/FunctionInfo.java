package nl.utwente.viskell.haskell.env;

import nl.utwente.viskell.haskell.type.Type;

public abstract class FunctionInfo {

    /** The function name. */
    private final String name;
    
    /** The type signature the corresponding function. */
    private final Type signature;

    /**
     * @param name The function name.
     * @param signature The type signature the corresponding function.
     */
    public FunctionInfo(String name, Type signature) {
        this.name = name;
        this.signature = signature;
    }

    /** @return The internal name of this function. */
    public final String getName() {
        return this.name;
    }

    /** @return The name of this function used for the front-end. */
    public String getDisplayName() {
        return this.name;
    }

    /** @return The a fresh copy of type signature of this function. */
    public final Type getFreshSignature() {
        return this.signature.getFresh();
    }

}