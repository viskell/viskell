package nl.utwente.viskell.model;

import com.google.common.collect.ImmutableList;

public abstract class Box implements ModelElement {
    /** X position of this Box in some unit */
    private float x;

    /** Y position of this Box in some unit */
    private float y;
    
    public abstract BoxGroup getDirectParent();
    
    public Component getParentComponent() {
        return this.getDirectParent().getParentComponent();
    }

    /** @return An immutable list of all input ports. */
    public abstract ImmutableList<InputPort> getInputs();

    /** @return An immutable list of all output ports. */
    public abstract ImmutableList<OutputPort> getOutputs();
    
    /** Set fresh types in all ports of this box for the next typechecking cycle. */
    protected abstract void refreshPortTypes();

    /** Updates the expression */
    protected abstract void updateExpr();

    /** @return the X coordinate of this Box's top-left corner. */
    public float getX() {
        return x;
    }

    /** @return the Y coordinate of this Box's top-left corner. */
    public float getY() {
        return y;
    }

    /** Set the position of this Box's top-left corner. */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
