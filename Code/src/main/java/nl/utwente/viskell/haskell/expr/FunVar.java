package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;

/*
 * A variable referring to known function
 */
public class FunVar extends Variable {

    /**
     * The information about the function being used.
     */
    private final FunctionInfo funInfo;
    
    /**
     * @param funInfo The information about the function being used.
     */
    public FunVar(FunctionInfo funInfo) {
        super(funInfo.getDisplayName());
        this.funInfo = funInfo;
    }

    @Override
    public Type inferType() throws HaskellTypeError {
        return this.funInfo.getFreshSignature();
    }

    @Override
    public String toString() {
        return this.name;
    }
    
    @Override
    public String toHaskell() {
        return funInfo.getName();
    }

}
