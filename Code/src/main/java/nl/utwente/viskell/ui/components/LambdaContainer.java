package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.expr.Annotated;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Lambda;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ComponentLoader;

/** Represent a lambda construct with internal anchor and blocks */
public class LambdaContainer extends BorderPane implements ComponentLoader {

    // TODO make this an independent class if other blocks (case?) need this too 
    /** An internal output anchor for an argument binder. */
    public class BinderAnchor extends OutputAnchor {

        // FIXME BinderAnchor should not have or use the DefinitionBlock parent
        public BinderAnchor(DefinitionBlock parent, Binder binder) {
            super(parent, binder);
        }

        @Override
        protected void extendExprGraph(LetExpression exprGraph) {
            return; // the scope of graph is limited its parent
        }
        
        @Override
        public void initiateConnectionChanges() {
            // Starts a new (2 phase) change propagation process from this lambda.
            LambdaContainer.this.handleConnectionChanges(false);
            LambdaContainer.this.handleConnectionChanges(true);
        }

        @Override
        public void prepareConnectionChanges() {
            LambdaContainer.this.refreshAnchorTypes();
        }

        @Override
        protected void handleConnectionChanges(boolean finalPhase) {
            LambdaContainer.this.handleConnectionChanges(finalPhase);
        }
    }

    // TODO make this an independent class if other blocks (case?) need this too
    /** An internal input anchor for a local result. */
    public class ResultAnchor extends InputAnchor {
        /** The optional type of the result of the function (the last part of the signature). */
        private final Optional<Type> resType;
        
        // FIXME ResultAnchor should not have or use the DefinitionBlock parent
        public ResultAnchor(DefinitionBlock parent, Optional<Type> resType) {
            super(parent);
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
        private void refreshAnchorType(TypeScope scope) {
            if (this.resType.isPresent()) {
                this.setFreshRequiredType(this.resType.get(), scope);
            } else {
                this.setFreshRequiredType(TypeScope.unique("y"), scope);
            }
        }

        @Override
        protected void handleConnectionChanges(boolean finalPhase) {
            LambdaContainer.this.handleConnectionChanges(finalPhase);
        }
    }
    
    /* area containing argument anchors */
    @FXML private Pane argSpace;
    
    /* area contain result anchor */
    @FXML private Pane resSpace;

    /* The definition block this lambda is contained by. */
    private DefinitionBlock wrapper;
    
    /* The lambda argument anchors */
    private List<BinderAnchor> args;

    /** The result anchor */
    private ResultAnchor res;

    /** Whether the anchor types are fresh*/
    private boolean freshAnchorTypes;
    
    /** Status of change updating process in this block. */
    private boolean updateInProgress;

    /**
     * Constructs a LambdaContainer for an untyped lambda of n arguments.
     * @param arity the number of arguments of this lambda.
     */
    public LambdaContainer(DefinitionBlock wrapper, int arity) {
        super();
        this.loadFXML("LambdaContainer");
        this.wrapper = wrapper;
        
        this.args = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            this.args.add(this.new BinderAnchor(wrapper, new Binder("x_" + i)));
        }
        this.res = this.new ResultAnchor(wrapper, Optional.empty());
        
        this.argSpace.getChildren().addAll(this.args);
        this.resSpace.getChildren().add(this.res);
    }
    
    /**
     * Constructs a LambdaContainer with explicit types.
     * @param name of the function.
     * @param type the full function type.
     */
    public LambdaContainer(DefinitionBlock wrapper, String name, Type type) {
        super();
        this.loadFXML("LambdaContainer");
        this.wrapper = wrapper;

        // Collect argument types and result type
        this.args = new ArrayList<>();
        Type t = type;
        int i = 0;
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            args.add(this.new BinderAnchor(wrapper, new Binder(name + "_x" + i, ft.getArgument())));
            i++;
            t = ft.getResult();
        }
        this.res = this.new ResultAnchor(wrapper, Optional.of(t));

        this.argSpace.getChildren().addAll(this.args);
        this.resSpace.getChildren().add(this.res);
    }

    /** Set fresh types in all anchors of this lambda for the next typechecking cycle. */
    protected void refreshAnchorTypes() {
        if (this.updateInProgress || this.freshAnchorTypes) {
            return; // refresh anchor types only once
        }
        this.freshAnchorTypes = true;
        
        TypeScope scope = new TypeScope();
        for (BinderAnchor arg : this.args) {
            arg.refreshType(scope);
        }
        this.res.refreshAnchorType(scope);
    }
    
    /** returns the current (inferred) type of this lambda */
    protected Type getLambdaType() {
        ArrayList<Type> types = new ArrayList<>();
        for (BinderAnchor arg : this.args) {
            types.add(arg.getType());
        }
        types.add(this.res.getType());

        return Type.fun(types.toArray(new Type[this.args.size()+1]));
    }
    
    /**
     * Handle the expression and types changes caused by modified connections or values.
     * Also propagate the changes through internal connected blocks, and then outwards.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    public final void handleConnectionChanges(boolean finalPhase) {
        if (this.updateInProgress != finalPhase) {
            return; // avoid doing extra work and infinite recursion
        }
        
        if (! finalPhase) {
            // in first phase ensure that anchor types are refreshed
            this.refreshAnchorTypes();
        }
        
        this.updateInProgress = !finalPhase;
        this.freshAnchorTypes = false;
        
        // first propagate up from the result anchor
        this.res.getConnection().ifPresent(c -> c.handleConnectionChangesUpwards(finalPhase));

        // also propagate in from above in case the lambda is partially connected 
        for (BinderAnchor arg : this.args) {
            for (InputAnchor anchor : arg.getOppositeAnchors()) {
                anchor.handleConnectionChanges(finalPhase);
                // take the type of argument connections in account even if the connected block is being processed
                anchor.getConnection().ifPresent(c -> c.handleConnectionChangesUpwards(finalPhase));
            }
        }

        // propagate internal type changes outwards
        this.wrapper.handleConnectionChanges(finalPhase);
    }
    
    /** @return The local expression this LambdaContainer represents. */
    public Expression getLocalExpr() {
        List<Binder> binders = this.args.stream().map(arg -> arg.binder).collect(Collectors.toList());
        LetExpression body = new LetExpression(this.res.getLocalExpr());
        this.res.extendExprGraph(body);
        return new Lambda(binders, body);
    }

    /** Called when the VisualState changed. */
    public void invalidateVisualState() {
        // TODO update anchors when they get a type label       
    }
    
}
