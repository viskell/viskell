package nl.utwente.viskell.haskell.env;

import java.util.Collections;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.components.DefinitionBlock;

/**
 * FunctionInfo generated from a DefinitionBlock
 */
public class DefinitionFunction extends FunctionInfo {
    
    /** The name to show to the user */
    protected String displayName;
    
    /**
     * Constructs the function info for a definition block.
     * @param source the accompanying definition block
     * @param displayName the visual name for this function
     * @param signature the type signature of the function
     */
    public DefinitionFunction(DefinitionBlock source, String displayName, Type signature) {
        super(source.getBinder().getUniqueName(), signature, Collections.singleton(source));
        this.displayName = displayName;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }

}
