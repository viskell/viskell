package nl.utwente.viskell.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class FunctionBox extends Box {
    
    private BoxGroup parent;
    
    private List<InputPort> inputs;
    
    private List<OutputPort> outputs;

    public FunctionBox(BoxGroup parent) {
        super();
        this.parent = parent;
        this.parent.addPart(this);
    }

    @Override
    public BoxGroup getDirectParent() {
        return this.parent;
    }

    @Override
    public List<InputPort> getInputs() {
        return ImmutableList.copyOf(inputs);
    }

    @Override
    public List<OutputPort> getOutputs() {
        return ImmutableList.copyOf(outputs);
    }

    @Override
    protected void refreshPortTypes() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateExpr() {
        // TODO Auto-generated method stub
    }
    
}
