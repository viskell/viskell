package nl.utwente.viskell.model;

public class Wire {
    
    private final SourcePort source;
    
    private final SinkPort sink;

    public Wire(SourcePort source, SinkPort sink) {
        this.source = source;
        this.sink = sink;
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

}
