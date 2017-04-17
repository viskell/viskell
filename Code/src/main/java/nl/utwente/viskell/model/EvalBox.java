package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.List;

public class EvalBox extends Box {
    
    private BoxGroup parent;
    
    private List<InputPort> inputs;
    
    public EvalBox(BoxGroup parent) {
        this.parent = parent;
        this.inputs = new ArrayList<>();
        this.parent.addPart(this);
    }

    @Override
    public BoxGroup getDirectParent() {
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

    @Override
    protected void refreshPortTypes() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateExpr() {
        // TODO Auto-generated method stub
    }

}
