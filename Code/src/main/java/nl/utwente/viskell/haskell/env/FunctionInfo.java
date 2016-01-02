package nl.utwente.viskell.haskell.env;

import java.util.Collection;
import java.util.Collections;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.components.Block;

public abstract class FunctionInfo {

    /** The function name. */
    private final String name;
    
    /** The type signature the corresponding function. */
    private final Type signature;
    
    /** A set of required blocks for this function type */
    private final Collection<Block> requiredBlocks;

    /**
     * @param name The function name.
     * @param signature The type signature the corresponding function.
     */
    public FunctionInfo(String name, Type signature) {
        this(name, signature, Collections.emptyList());
    }

    /**
     * @param name The function name.
     * @param signature The type signature the corresponding function.
     * @param requiredBlocks A set of blocks required for this function type
     */
    protected FunctionInfo(String name, Type signature, Collection<Block> requiredBlocks) {
        this.name = name;
        this.signature = signature;
        this.requiredBlocks = requiredBlocks;
    }

    /** @return The internal name of this function. */
    public final String getName() {
        return this.name;
    }

    /** @return The name of this function used for the front-end. */
    public String getDisplayName() {
        return getName();
    }

    /** @return The a fresh copy of type signature of this function. */
    public final Type getFreshSignature() {
        return this.signature.getFresh();
    }
    
    /** @return The required blocks for this function type */
    public Collection<Block> getRequiredBlocks() {
        return requiredBlocks;
    }

    /** @return the number of argument this function can take. */
    public int argumentCount() {
        return this.signature.countArguments();
    }

}
