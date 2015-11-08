package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.List;

import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.LocalVar;
import nl.utwente.viskell.haskell.expr.Variable;
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
    
    public boolean hasWire() {
        return !this.wires.isEmpty();
    }

    public void addWire(Wire wire) {
        this.wires.add(wire);
    }

    public void dropWire(Wire wire) {
        this.wires.remove(wire);
    }

    public void dropAllWires() {
        this.wires = new ArrayList<>();
    }

    public Type getType() {
        return this.type;
    }
    
    public Variable getVariable() {
        return new LocalVar(this.binder);
    }
}
