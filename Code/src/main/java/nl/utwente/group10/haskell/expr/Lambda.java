package nl.utwente.group10.haskell.expr;

import java.util.IdentityHashMap;
import java.util.List;

import com.google.common.collect.Lists;

import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.type.FunType;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeVar;

public class Lambda extends Expression {

    /** The list of variable binders in this lambda */
    private final List<Binder> binders;
    
    /** The expression in the body of this lambda */ 
    private final Expression body;
    
    /**
     * @param binders The list of variable binders in this lambda, should be at least one
     * @param body The expression in the body of this lambda
     */
    public Lambda(List<Binder> binders, Expression body) {
        this.binders = binders;
        this.body = body;
    }

    @Override
    protected Type inferType() throws HaskellException {
        // Rule [Abs]:
        // assign the binder fresh type variable (x)
        IdentityHashMap<TypeVar.TypeInstance, TypeVar> staleToFresh = new IdentityHashMap<>();
        for (Binder x : this.binders) {
            x.refreshBinderType(staleToFresh);
        }
        // infer the type (y) for the body with the type variable in the context
        Type type = this.body.inferType();
        // then lambda has the function type (x -> y)
        for (Binder x : Lists.reverse(this.binders)) {
            type = new FunType(x.getBoundType(), type);
        }
        
        return type;
    }

    @Override
    public String toHaskell() {
        StringBuilder out = new StringBuilder();
        out.append("(\\");

        for (Binder x : this.binders) {
            out.append(" ").append(x.getUniqueName());
        }

        out.append(" -> ");
        out.append(this.body.toHaskell());
        out.append("");
        return out.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("\\");

        for (Binder x : this.binders) {
            out.append(" ").append(x.getBaseName());
        }

        out.append(" -> ");
        out.append(this.body.toString());
        return out.toString();
    }

}
