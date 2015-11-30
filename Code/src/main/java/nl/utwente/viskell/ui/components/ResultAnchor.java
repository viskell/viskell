package nl.utwente.viskell.ui.components;

import java.util.Optional;

import nl.utwente.viskell.haskell.expr.Annotated;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;

// TODO make this an independent class if other blocks (case?) need this too
/** An internal input anchor for a local result. */
public class ResultAnchor extends InputAnchor {
    /**
     * 
     */
    private final LambdaContainer lambdaContainer;
    /** The optional type of the result of the function (the last part of the signature). */
    private final Optional<Type> resType;
    
    // FIXME ResultAnchor should not have or use the DefinitionBlock parent
    public ResultAnchor(LambdaContainer lambdaContainer, Block parent, Optional<Type> resType) {
        super(parent);
        this.lambdaContainer = lambdaContainer;
        this.resType = resType;
    }
    
    @Override
    public Expression getLocalExpr() {
        if (this.resType.isPresent()) {
            return new Annotated(super.getLocalExpr(), this.resType.get());
        }
       
        return super.getLocalExpr();
    }
    
    /** Set fresh type for the next typechecking cycle.*/
    protected void refreshAnchorType(TypeScope scope) {
        if (this.resType.isPresent()) {
            this.setFreshRequiredType(this.resType.get(), scope);
        } else {
            this.setFreshRequiredType(TypeScope.unique("y"), scope);
        }
    }

    @Override
    protected void handleConnectionChanges(boolean finalPhase) {
        this.lambdaContainer.handleConnectionChanges(finalPhase);
    }
}