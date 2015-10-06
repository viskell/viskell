package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeVar;

/*
 * A Hole represents an yet unknown gap in an Expression or an open input connection
 */
public class Hole extends Expression {

    private final TypeVar typevar;
    
    public Hole() {
        this.typevar = Type.var("holey");
    }

    @Override
    protected Type inferType() {
        return typevar;
    }

    @Override
    public String toHaskell() {
        return "(error \"Nothing to see yet in this hole\")";
    }

    @Override
    public String toString() {
        return "<<hole>>";
    }

}
