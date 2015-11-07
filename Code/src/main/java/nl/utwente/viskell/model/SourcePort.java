package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.List;

import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;

public class SourcePort {
    
    private List<Wire> wires;
    
    private Type type;
    
    private Binder binder;

    public SourcePort(Binder binder) {
        this.wires = new ArrayList<>();
        this.type = TypeScope.unique("!!!");
        this.binder = binder;
    }
    
}
