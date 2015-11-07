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
    
}
