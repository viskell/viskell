package nl.utwente.viskell.ui.components;

import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.LetExpression;

// TODO make this an independent class if other blocks (case?) need this too 
/** An internal output anchor for an argument binder. */
public class BinderAnchor extends OutputAnchor {

    /**
     * 
     */
    private final LambdaContainer lambdaContainer;

    // FIXME BinderAnchor should not have or use the DefinitionBlock parent
    public BinderAnchor(LambdaContainer lambdaContainer, Block parent, Binder binder) {
        super(parent, binder);
        this.lambdaContainer = lambdaContainer;
    }

    @Override
    protected void extendExprGraph(LetExpression exprGraph) {
        return; // the scope of graph is limited its parent
    }
    
    @Override
    public void initiateConnectionChanges() {
        // Starts a new (2 phase) change propagation process from this lambda.
        this.lambdaContainer.handleConnectionChanges(false);
        this.lambdaContainer.handleConnectionChanges(true);
    }

    @Override
    public void prepareConnectionChanges() {
        this.lambdaContainer.refreshAnchorTypes();
    }

    @Override
    protected void handleConnectionChanges(boolean finalPhase) {
        this.lambdaContainer.handleConnectionChanges(finalPhase);
    }
}