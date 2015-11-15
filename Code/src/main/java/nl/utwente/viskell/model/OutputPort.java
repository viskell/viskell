package nl.utwente.viskell.model;

import nl.utwente.viskell.haskell.expr.Binder;

public class OutputPort extends SourcePort {
    
    private final Box box;

    public OutputPort(Box box, Binder binder) {
        super(binder);
        this.box = box;
    }

    public Box getBox() {
        return box;
    }
}
