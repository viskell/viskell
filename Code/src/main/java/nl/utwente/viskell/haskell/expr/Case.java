package nl.utwente.viskell.haskell.expr;

import java.util.List;

import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;

public class Case extends Expression {
    
    private List<Alternative> alternatives;
    private Expression expression;
    
    public static final class Alternative {
        private final Binder pattern;
        private final LetExpression guards;
        
        public Alternative(Binder pattern, LetExpression guards) {
            this.pattern = pattern;
            this.guards = guards;
        }
    }
    
    public Case(Expression e, List<Alternative> alternatives) {
        expression = e;
        this.alternatives = alternatives;
    }

    @Override
    public Type inferType() throws HaskellTypeError {
        return null;
    }

    @Override
    public String toHaskell() {
        StringBuilder sb = new StringBuilder();
        sb.append("case "+expression.toHaskell()+" of {");
        alternatives.stream().forEach(alternative -> {
            sb.append(alternative.pattern.getUniqueName()+" | "+alternative.guards.toHaskell()+"; ");
        });
        sb.append("}");
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return toHaskell();
    }

}
