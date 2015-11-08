package nl.utwente.viskell.haskell.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;

public class LetExpression extends Expression {

    private LinkedHashMap<Binder, Expression> binders;
    
    private Expression body;
    
    public LetExpression(Expression body) {
        super();
        this.body = body;
        this.binders = new LinkedHashMap<>();
    }

    public boolean prepend(Binder binder, Expression expr) {
        if (this.binders.containsKey(binder)) {
            // remove the old entry to preserve least recent insertion ordering
            this.binders.remove(binder);
            this.binders.put(binder, expr);
            return false;
        }
        
        this.binders.put(binder, expr);
        return true;
    }
    
    @Override
    protected Type inferType() throws HaskellTypeError {
        return this.body.inferType();
    }

    @Override
    public String toHaskell() {
        StringBuilder builder = new StringBuilder();
        this.binders.forEach((v, x) -> builder.insert(0, v.getUniqueName() + " = " + x.toHaskell() + "; "));
        builder.insert(0, "let {");
        builder.append("} in ");
        builder.append(this.body.toHaskell());
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.binders.forEach((v, x) -> builder.insert(0, "    " +v.getUniqueName() + " = " + x.toHaskell() + ";\n"));
        builder.insert(0, "let {\n");
        builder.append("  }\n in ");
        builder.append(this.body.toHaskell());
        return builder.toString();
    }

    @Override
    public final List<Expression> getChildren() {
        List<Expression> exprs = new ArrayList<>();
        exprs.add(this.body);
        exprs.addAll((this.binders.values()));
        Collections.reverse(exprs);
        return exprs;
    }
    
}
