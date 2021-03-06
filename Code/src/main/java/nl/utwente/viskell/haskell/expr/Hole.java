package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.haskell.type.TypeVar;

/*
 * A Hole represents an yet unknown gap in an Expression or an open input connection
 */
public class Hole extends Variable {

    private final TypeVar typevar;
    
    public Hole() {
        super("<<hole>>");
        this.typevar = TypeScope.unique("holey");
    }

    @Override
    public Type inferType() {
        return typevar;
    }

    @Override
    public String toHaskell() {
        return "(error \"Open expression\")";
    }

    @Override
    public String toString() {
        return "<<hole>>";
    }

}
