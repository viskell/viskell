package nl.utwente.viskell.model;

import com.google.common.collect.ImmutableList;
import nl.utwente.viskell.haskell.expr.Binder;

public class LambdaBox extends Box {
    
    private BoxGroup parent;
    
    private Component body;
    
    private final OutputPort funRes;

    public LambdaBox(BoxGroup parent, Component body) {
        super();
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
    public ImmutableList<InputPort> getInputs() {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<OutputPort> getOutputs() {
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
