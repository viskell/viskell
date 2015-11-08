package nl.utwente.viskell.model;

import java.util.Collections;
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
        return Collections.unmodifiableList(this.inputs);
    }

    @Override
    public List<OutputPort> getOutputs() {
        return Collections.unmodifiableList(this.outputs);
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
