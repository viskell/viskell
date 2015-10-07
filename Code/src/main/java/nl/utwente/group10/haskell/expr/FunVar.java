package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.FunctionInfo;
import nl.utwente.group10.haskell.type.HaskellTypeError;
import nl.utwente.group10.haskell.type.Type;

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
        super(funInfo.getName());
        this.funInfo = funInfo;
    }

    @Override
    protected Type inferType() throws HaskellTypeError {
        return this.funInfo.getFreshSignature();
    }

    @Override
    public String toString() {
        return this.name;
    }

}
