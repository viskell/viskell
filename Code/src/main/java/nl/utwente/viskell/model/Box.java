package nl.utwente.viskell.model;

import java.util.List;

public interface Box {
    
    public Grouping getDirectParent();
    
    public default Component getParentComponent() {
        Grouping parent = this.getDirectParent();
        while (true) {
            if (parent instanceof Component) {
                return (Component)parent;
            } else {
                parent = ((Selection)parent).getParent();
            }
        } 
    }

    /** @return An immutable list of all input ports. */
    public List<InputPort> getInputs();

    /** @return An immutable list of all output ports. */
    public List<OutputPort> getOutputs();
    
}
