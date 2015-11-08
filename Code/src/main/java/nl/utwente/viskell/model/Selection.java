package nl.utwente.viskell.model;

public class Selection extends Grouping {

    public Grouping parent;

    public Selection(Grouping parent) {
        super();
        this.parent = parent;
    }

    public Grouping getParent() {
        return this.parent;
    }

    
}
