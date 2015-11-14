package nl.utwente.viskell.model;

import com.google.common.collect.ImmutableList;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;

import java.util.List;

public class ConstantBox extends Box {
    
    private BoxGroup parent;
    
    private Expression expr;
    
    private Type type;
    
    private final OutputPort output;

    public ConstantBox(BoxGroup parent, String name, Expression expr, Type type) {
        super();
        this.parent = parent;
        this.expr = expr;
        this.type = type;
        this.output = new OutputPort(this, new Binder(name));
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
        return ImmutableList.of(this.output);
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
