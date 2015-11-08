package nl.utwente.viskell.model;

import java.util.Optional;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;

public class SinkPort {
    
    private Optional<Wire> wire;
    
    private Type type;

    public SinkPort() {
        this.wire = Optional.empty();
        this.type = TypeScope.unique("???");
    }
    
    public boolean hasWire() {
        return this.wire.isPresent();
    }
    
    public void dropWire() {
        this.wire = Optional.empty();
    }
    
    public void setWire(Wire wire) {
        this.wire = Optional.of(wire);
    }
    
    public Type getType() {
        return this.type;
    }
}
