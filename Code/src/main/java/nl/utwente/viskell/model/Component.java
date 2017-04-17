package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Component extends BoxGroup {
    
    private Optional<Box> wrapper;
    
    private List<SourcePort> sources;
    
    private List<SinkPort> sinks;

    public Component() {
        super();
        this.sources = new ArrayList<>();
        this.sinks = new ArrayList<>();
    }

    public Optional<Box> getWrapper() {
        return wrapper;
    }

    public void setWrapper(Box wrapper) {
        this.wrapper = Optional.of(wrapper);
    }
    
}
