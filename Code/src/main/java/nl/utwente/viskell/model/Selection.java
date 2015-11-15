package nl.utwente.viskell.model;

public class Selection extends BoxGroup {

    private BoxGroup parent;

    public Selection(BoxGroup parent) {
        super();
        this.parent = parent;
    }

    public BoxGroup getParent() {
        return this.parent;
    }

    @Override
    public void accept(ModelVisitor visitor) {
        visitor.visit(this);
    }
}
