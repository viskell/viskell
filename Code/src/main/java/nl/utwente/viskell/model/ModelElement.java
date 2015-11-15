package nl.utwente.viskell.model;

public interface ModelElement {
    public void accept(ModelVisitor visitor);
}
