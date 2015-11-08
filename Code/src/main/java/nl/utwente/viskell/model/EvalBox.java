package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.List;

public class EvalBox implements Box {
    
    private Grouping parent;
    
    private List<InputPort> inputs;
    
    public EvalBox(Grouping parent) {
        this.parent = parent;
        this.inputs = new ArrayList<>();
        this.parent.addPart(this);
    }

    @Override
    public Grouping getDirectParent() {
        return parent;
    }

    @Override
    public List<InputPort> getInputs() {
        return inputs;
    }

    @Override
    public List<OutputPort> getOutputs() {
        return new ArrayList<>();
    }

}
