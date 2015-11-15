package nl.utwente.viskell.model;

import java.util.Optional;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;

public class SinkPort implements ModelElement {
    
    private Optional<Wire> wire;
    
    private Type type;

    public SinkPort() {
        super();
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
    
    /** @return Optional of the wire's opposite sink port.  */
    public Optional<SourcePort> getOppositePort() {
        return this.wire.map(w -> w.getSource());
    }

    @Override
    public void accept(ModelVisitor visitor) {
        visitor.visit(this);
        wire.ifPresent(w -> w.accept(visitor));
    }
}
