package nl.utwente.viskell.model;

public class Wire {
    
    private final SourcePort source;
    
    private final SinkPort sink;

    public Wire(SourcePort source, SinkPort sink) {
        this.source = source;
        this.sink = sink;
    }

}
