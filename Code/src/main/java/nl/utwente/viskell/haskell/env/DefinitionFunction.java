package nl.utwente.viskell.haskell.env;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.components.DefinitionBlock;

/**
 * FunctionInfo generated from a DefinitionBlock
 */
public class DefinitionFunction extends FunctionInfo {
    
    /** The name to show to the user */
    protected String displayName;
    
    /** The DefinitionBlock to which this info belongs */
    protected DefinitionBlock source;

    public DefinitionFunction(DefinitionBlock source, String displayName, Type signature) {
        super(source.getBinder().getUniqueName(), signature);
        this.displayName = displayName;
        this.source = source;
    }
    
    /** @return The DefinitionBlock to which this info belongs */
    public DefinitionBlock getSource() {
        return source;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }

}
