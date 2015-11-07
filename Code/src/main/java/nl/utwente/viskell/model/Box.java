package nl.utwente.viskell.model;

import java.util.List;

public interface Box {
    
    public Grouping getParent();

    public List<InputPort> getInputs();

    public List<OutputPort> getOutputs();
    
}
