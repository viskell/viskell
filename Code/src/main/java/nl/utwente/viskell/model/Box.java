package nl.utwente.viskell.model;

import java.util.List;

public abstract class Box {
    
    public abstract BoxGroup getDirectParent();
    
    public Component getParentComponent() {
        return this.getDirectParent().getParentComponent();
    }

    /** @return An immutable list of all input ports. */
    public abstract List<InputPort> getInputs();

    /** @return An immutable list of all output ports. */
    public abstract List<OutputPort> getOutputs();
    
    /** Set fresh types in all ports of this box for the next typechecking cycle. */
    protected abstract void refreshPortTypes();

    /** Updates the expression */
    protected abstract void updateExpr();

}
