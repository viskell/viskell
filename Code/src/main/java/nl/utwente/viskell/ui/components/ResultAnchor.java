package nl.utwente.viskell.ui.components;

import java.util.Optional;
import java.util.Set;

import nl.utwente.viskell.haskell.expr.Annotated;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.BlockContainer;

/** An internal input anchor for a local result. */
public class ResultAnchor extends InputAnchor {
    
    private final WrappedContainer container;
    
    /** The optional type of the result of the function (the last part of the signature). */
    private final Optional<Type> resType;
    
    // FIXME ResultAnchor should not have or use the DefinitionBlock parent
    public ResultAnchor(WrappedContainer container, Block parent, Optional<Type> resType) {
        super(parent);
        this.container = container;
        this.resType = resType;
    }
    
    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        if (resType.isPresent()) {
            return new Annotated(super.getLocalExpr(outsideAnchors), resType.get());
        }
       
        return super.getLocalExpr(outsideAnchors);
    }
    
    /** Set fresh type for the next typechecking cycle.*/
    protected void refreshAnchorType(TypeScope scope) {
        if (resType.isPresent()) {
            setFreshRequiredType(resType.get(), scope);
        } else {
            setFreshRequiredType(new TypeScope().getVar("x"), scope);
        }
    }

    @Override
    protected void handleConnectionChanges(boolean finalPhase) {
        container.handleConnectionChanges(finalPhase);
    }
    
    @Override
    public BlockContainer getContainer() {
        return this.container;
    }
    
}
