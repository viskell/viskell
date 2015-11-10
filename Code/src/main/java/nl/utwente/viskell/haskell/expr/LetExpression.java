package nl.utwente.viskell.haskell.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;

public class LetExpression extends Expression {

    /** An ordered map of let bindings consisting of binder and expression pairs. */
    private final LinkedHashMap<Binder, Expression> binders;
    
    /** The expression forming the body of this let expression*/
    private final Expression body;
    
    /**
     * Constructs an empty let expression (with no local bindings).
     * @param body the main expression of this let.
     */
    public LetExpression(Expression body) {
        super();
        this.body = body;
        this.binders = new LinkedHashMap<>();
    }

    /** @return The body of this let expression */
    public Expression getBody() {
        return this.body;
    }
    
    /**
     * Extends a let expression with an extra binding.
     * Preserves ordering of let bindings and avoids duplicate binders.
     * @param binder the binder variable for the extra let binding.
     * @param expr the bound subexpression for the extra let binding.
     * @return whether a extra let binding has added to this let expression.
     */
    public boolean addLetBinding(Binder binder, Expression expr) {
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
    public Type inferType() throws HaskellTypeError {
        // TODO the binders should be typechecked first
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
