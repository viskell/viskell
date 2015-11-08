package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.List;

public abstract class BoxGroup {
    
    private List<Box> parts;

    public BoxGroup() {
        this.parts = new ArrayList<>();
    }

    public void addPart(Box box) {
        this.parts.add(box);
    }
    
    public Component getParentComponent() {
        BoxGroup parent = this;
        while (true) {
            if (parent instanceof Component) {
                return (Component)parent;
            } else {
                parent = ((Selection)parent).getParent();
            }
        } 
    }
    
}
