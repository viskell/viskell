package nl.utwente.viskell.model;

public class Wire {
    
    private final SourcePort source;
    
    private final SinkPort sink;
    
    private boolean hasTypeError;

    private Wire(SourcePort source, SinkPort sink) {
        super();
        this.source = source;
        this.sink = sink;
        this.hasTypeError = false;
    }

    /** @return the source port of this wire */
    public SourcePort getSource() {
        return this.source;
    }

    /** @return the sink port of this wire */
    public SinkPort getSink() {
        return this.sink;
    }
    
    public void remove() {
        this.source.dropWire(this);
        this.sink.dropWire();
    }

    public static Wire connect(SourcePort source, SinkPort sink) {
        Wire wire = new Wire(source, sink);
        source.addWire(wire);
        sink.setWire(wire);
        return wire;
    }

}
