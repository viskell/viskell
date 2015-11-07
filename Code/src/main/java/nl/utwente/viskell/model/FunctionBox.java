package nl.utwente.viskell.model;

import java.util.List;

public class FunctionBox implements Box {
    
    private Grouping parent;
    
    private List<InputPort> inputs;
    
    private List<OutputPort> outputs;

    public FunctionBox(Grouping parent) {
        super();
        this.parent = parent;
    }

    @Override
    public Grouping getParent() {
        return this.parent;
    }

    @Override
    public List<InputPort> getInputs() {
        return this.inputs;
    }

    @Override
    public List<OutputPort> getOutputs() {
        return this.outputs;
    }
    
}
