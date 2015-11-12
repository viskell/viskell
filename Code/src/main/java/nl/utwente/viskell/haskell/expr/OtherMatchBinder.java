package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.haskell.type.Type;

public class OtherMatchBinder extends Binder {

    public OtherMatchBinder(String name, Type annotation) {
        super(name, annotation);
    }

    public OtherMatchBinder(String name) {
        super(name);
    }

}
