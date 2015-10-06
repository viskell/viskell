package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.HaskellTypeError;
import nl.utwente.group10.haskell.type.Type;

/**
 * A variable that is locally bound, by for example a lambda 
 */
public class LocalVar extends Variable {

    /** The binder this variable is bound to */
    private final Binder binder;

    /**
     * @param binder where this variable is bound to
     */
    public LocalVar(Binder binder) {
        super(binder.getUniqueName());
        this.binder = binder;
    }

    @Override
    protected Type inferType() throws HaskellTypeError {
        return binder.getBoundType(this);
    }

    @Override
    public String toHaskell() {
        return binder.getUniqueName();
    }

    @Override
    public String toString() {
        return binder.getBaseName();
    }

}
