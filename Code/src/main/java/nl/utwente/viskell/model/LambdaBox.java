package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.utwente.viskell.haskell.expr.Binder;

public class LambdaBox implements Box {
    
    private Grouping parent;
    
    private Component body;
    
    private final OutputPort funRes;

    public LambdaBox(Grouping parent, Component body) {
        this.parent = parent;
        this.body = body;
        this.body.setWrapper(this);
        this.funRes = new OutputPort(this, new Binder("lam"));
        this.parent.addPart(this);
    }

    @Override
    public Grouping getDirectParent() {
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
