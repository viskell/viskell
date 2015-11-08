package nl.utwente.viskell.model;

import java.util.Collections;
import java.util.List;

public class FunctionBox implements Box {
    
    private Grouping parent;
    
    private List<InputPort> inputs;
    
    private List<OutputPort> outputs;

    public FunctionBox(Grouping parent) {
        super();
        this.parent = parent;
        this.parent.addPart(this);
    }

    @Override
    public Grouping getDirectParent() {
        return this.parent;
    }

    @Override
    public List<InputPort> getInputs() {
        return Collections.unmodifiableList(this.inputs);
    }

    @Override
    public List<OutputPort> getOutputs() {
        return Collections.unmodifiableList(this.outputs);
    }
    
}
