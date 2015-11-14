package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;

public class ConstantBox extends Box {
    
    private BoxGroup parent;
    
    private Expression expr;
    
    private Type type;
    
    private final OutputPort output;

    public ConstantBox(BoxGroup parent, String name, Expression expr, Type type) {
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
        return new ArrayList<>();
    }

    @Override
    public List<OutputPort> getOutputs() {
        return Collections.singletonList(this.output);
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
