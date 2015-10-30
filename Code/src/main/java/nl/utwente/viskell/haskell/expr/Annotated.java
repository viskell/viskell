package nl.utwente.viskell.haskell.expr;

import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeChecker;

/**
 * An Expression be annotated (restricted) by a type, in Haskell notation it is "(expr :: type)".
 */
public class Annotated extends Expression {

    /** The expression being annotated by a type. */
    final Expression expr;
    
    /** The type that this expression is restricted to. */
    final Type annotation;

    /**
     * @param expr the expression being annotated.
     * @param annotation the type that this expression is restricted to.
     */
    public Annotated(Expression expr, Type annotation) {
        this.expr = expr;
        this.annotation = annotation;
    }

    @Override
    protected Type inferType() throws HaskellTypeError {
        Type type = this.expr.findType();
        TypeChecker.unify(this, type, this.annotation.getFresh());
        return type;
    }

    @Override
    public String toHaskell() {
        // FIXME for now we do not add the " :: type" part because of incompatible type representation. 
        return this.expr.toHaskell();
    }

    @Override
    public String toString() {
        return this.expr.toString() + " :: " + this.annotation.toString();
    }

}
