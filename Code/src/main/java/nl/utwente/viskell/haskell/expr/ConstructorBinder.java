package nl.utwente.viskell.haskell.expr;

import java.util.List;

/** A binder that decompose a data constructor into a list of subbinders. */
public class ConstructorBinder extends Binder {
    
    /** The data constructor name. */
    private final String constructor;
    
    /** A list of Binders for each constructor element */
    private final List<Binder> binders;
    
    public ConstructorBinder(String constr, List<Binder> binders) {
        super("none", null);
        this.binders = binders;
        this.constructor = constr;
    }
    
    @Override
    public String getUniqueName() {
        return binders.stream().map(Binder::getUniqueName).reduce(constructor, (str, bname) -> str+" "+bname);
    }
}
