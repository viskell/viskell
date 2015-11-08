package nl.utwente.viskell.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Grouping {
    
    private List<Box> parts;

    public Grouping() {
        this.parts = new ArrayList<>();
    }

    public void addPart(Box box) {
        this.parts.add(box);
    }
    
}
