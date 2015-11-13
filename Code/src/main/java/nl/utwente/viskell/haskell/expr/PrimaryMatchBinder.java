package nl.utwente.viskell.haskell.expr;

import java.util.ArrayList;
import java.util.List;

public class PrimaryMatchBinder extends Binder {
    
    protected List<Binder> binders;
    protected String constructor;
    
    public PrimaryMatchBinder(String constr) {
        super("none", null);
        
        binders = new ArrayList<>();
        constructor = constr;
    }
    
    public void addBinder(Binder binder) {
        binders.add(binder);
    }
    
    @Override
    public String getUniqueName() {
        return binders.stream().map(Binder::getUniqueName).reduce(constructor, (str, bname) -> str+" "+bname);
    }
}
