package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.List;

public class Component extends Grouping {
    
    private List<SourcePort> sources;
    
    private List<SinkPort> sinks;

    public Component() {
        super();
        this.sources = new ArrayList<>();
        this.sinks = new ArrayList<>();
    }
    
}
