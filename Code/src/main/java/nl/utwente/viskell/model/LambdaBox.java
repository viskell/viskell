package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LambdaBox implements Box {
    
    private Grouping parent;
    
    private Component body;
    
    private OutputPort funRes;

    public LambdaBox(Grouping parent, Component body) {
        this.parent = parent;
        this.body = body;
    }

    @Override
    public Grouping getParent() {
        return this.parent;
    }

    @Override
    public List<InputPort> getInputs() {
        return new ArrayList<>();
    }

    @Override
    public List<OutputPort> getOutputs() {
        return Collections.singletonList(this.funRes);
    }
    
}
