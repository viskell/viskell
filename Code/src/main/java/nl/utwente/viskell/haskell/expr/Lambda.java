package nl.utwente.viskell.haskell.expr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;

import java.util.List;

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
    public Type inferType() throws HaskellTypeError {
        TypeScope scope = new TypeScope();
        // Rule [Abs]:
        // assign the binder fresh type variable (x)
        for (Binder x : this.binders) {
            x.refreshBinderType(scope);
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
        
        if (this.binders.isEmpty()) {
            out.append("_");
        }

        out.append(" -> ");
        out.append(this.body.toHaskell());
        out.append(")");
        
        if (this.binders.isEmpty()) {
            out.append(" ()");
        }

        return out.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("(\\");

        for (Binder x : this.binders) {
            out.append(" ").append(x.getBaseName());
        }
        
        if (this.binders.isEmpty()) {
            out.append("_");
        }

        out.append(" -> ");
        out.append(this.body.toString());
        out.append(")");
        
        if (this.binders.isEmpty()) {
            out.append(" ()");
        }

        return out.toString();
    }

    @Override
    public List<Expression> getChildren() {
        return ImmutableList.of(body);
    }
}
