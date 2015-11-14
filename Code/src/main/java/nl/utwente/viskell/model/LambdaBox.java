package nl.utwente.viskell.model;

import com.google.common.collect.ImmutableList;
import nl.utwente.viskell.haskell.expr.Binder;

import java.util.List;

public class LambdaBox extends Box {
    
    private BoxGroup parent;
    
    private Component body;
    
    private final OutputPort funRes;

    public LambdaBox(BoxGroup parent, Component body) {
        this.parent = parent;
        this.body = body;
        this.body.setWrapper(this);
        this.funRes = new OutputPort(this, new Binder("lam"));
        this.parent.addPart(this);
    }

    @Override
    public BoxGroup getDirectParent() {
        return this.parent;
    }

    @Override
    public List<InputPort> getInputs() {
        return ImmutableList.of();
    }

    @Override
    public List<OutputPort> getOutputs() {
        return ImmutableList.of(this.funRes);
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
