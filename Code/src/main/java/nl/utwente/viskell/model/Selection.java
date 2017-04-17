package nl.utwente.viskell.model;

public class Selection extends BoxGroup {

    public BoxGroup parent;

    public Selection(BoxGroup parent) {
        super();
        this.parent = parent;
    }

    public BoxGroup getParent() {
        return this.parent;
    }

    
}
