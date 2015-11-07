package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;

public class ConstantBox implements Box {
    
    private Grouping parent;
    
    private Expression expr;
    
    private Type type;
    
    private OutputPort ouput;

    public ConstantBox(Component parent, String name, Expression expr, Type type) {
        this.parent = parent;
        this.expr = expr;
        this.type = type;
        this.ouput = new OutputPort(this, new Binder(name));
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
        return Collections.singletonList(this.ouput);
    }

}
