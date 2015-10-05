package nl.utwente.group10.haskell.expr;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.Type;

/*
 * A Hole represents an yet unknown gap in an Expression or an open input connection
 */
public class Hole extends Expr {

    // TODO maybe it is usefull to add some of (unique) id to each Hole
    
    public Hole() {
    }

    @Override
    public Type analyze(Env env) throws HaskellException {
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
