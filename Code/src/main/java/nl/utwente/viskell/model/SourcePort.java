package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.LocalVar;
import nl.utwente.viskell.haskell.expr.Variable;
import nl.utwente.viskell.haskell.type.Type;

public class SourcePort {
    
    private List<Wire> wires;
    
    private Binder binder;

    public SourcePort(Binder binder) {
        this.wires = new ArrayList<>();
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
        return null; // TODO //this.binder.getBoundType();
    }
    
    public Variable getVariable() {
        return new LocalVar(this.binder);
    }
    
    /** @return A list of each sink ports for each wire this port has. */
   public List<SinkPort> getOppositePorts() {
       return this.wires.stream().map(w -> w.getSink()).collect(Collectors.toList());
   }

}
