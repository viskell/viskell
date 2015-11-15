package nl.utwente.viskell.model;

import com.google.common.collect.ImmutableList;
import nl.utwente.viskell.haskell.expr.Binder;

import java.util.ArrayList;
import java.util.List;

public class FunctionBox extends Box {
    
    private BoxGroup parent;
    
    private List<InputPort> inputs;
    
    private List<OutputPort> outputs;

    public FunctionBox(BoxGroup parent) {
        super();
        this.parent = parent;
        this.parent.addPart(this);

        inputs = new ArrayList<>();
        outputs = new ArrayList<>();

        // TODO other arities
        inputs.add(new InputPort(this));
        inputs.add(new InputPort(this));
        outputs.add(new OutputPort(this, new Binder("fn")));
    }

    @Override
    public BoxGroup getDirectParent() {
        return this.parent;
    }

    @Override
    public ImmutableList<InputPort> getInputs() {
        return ImmutableList.copyOf(inputs);
    }

    @Override
    public ImmutableList<OutputPort> getOutputs() {
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
