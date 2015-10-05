package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Environment;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.Type;

/*
 * A Hole represents an yet unknown gap in an Expression or an open input connection
 */
public class Hole extends Expression {

    // TODO maybe it is useful to add some of (unique) id to each Hole
    
    public Hole() {
    }

    @Override
    protected Type inferType(Environment env) throws HaskellException {
        return Type.var("holey");
    }

    @Override
    public String toHaskell() {
        return "undefined";
    }

    @Override
    public String toString() {
        return "<<hole>>";
    }

}
