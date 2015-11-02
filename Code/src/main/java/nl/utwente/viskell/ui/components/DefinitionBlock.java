package nl.utwente.viskell.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.viskell.haskell.expr.Annotated;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Lambda;
import nl.utwente.viskell.haskell.expr.LocalVar;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.CustomUIPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A definition block is a block that represents a named lambda. It can be used to build lambda abstractions.
 */
public class DefinitionBlock extends Block implements ComponentLoader {

    // TODO make this an independent class if other blocks (case?) need this too 
    /** An internal output anchor for an argument binder. */
    public static class BinderAnchor extends OutputAnchor {
        /** The variable binder corresponding to this input argument */
        private final Binder binder;

        public BinderAnchor(DefinitionBlock parent, Binder binder) {
            super(parent);
            this.binder = binder;
        }

        @Override
        public Expression getExpr() {
            return new LocalVar(this.binder);
        }
        
        /** Set fresh type for the next typechecking cycle.*/
        private void refreshAnchorType(TypeScope scope) {
            this.setType(this.binder.refreshBinderType(scope));
        }
    }

    // TODO make this an independent class if other blocks (case?) need this too
    /** An internal input anchor for a local result. */
    public static class ResultAnchor extends InputAnchor {
        /** The optional type of the result of the function (the last part of the signature). */
        private final Optional<Type> resType;

        public ResultAnchor(DefinitionBlock parent, Optional<Type> resType) {
            super(parent);
            this.resType = resType;
        }
        
        @Override
        public Expression getExpr() {
            if (this.resType.isPresent()) {
                return new Annotated(super.getExpr(), this.resType.get());
            }
           
            return super.getExpr();
        }
        
        /** Set fresh type for the next typechecking cycle.*/
        private void refreshAnchorType(TypeScope scope) {
            if (this.resType.isPresent()) {
                this.setType(this.resType.get().getFresh(scope));
            } else {
                this.setType(TypeScope.unique("λ_res"));
            }
        }
    }
    
    @FXML private Pane argSpace;
    @FXML private Pane resSpace;
    @FXML private Pane funSpace;

    @FXML private Label signature;

    private List<BinderAnchor> args;

    /** The result anchor (first bottom anchor) */
    private ResultAnchor res;

    /** The function anchor (second bottom anchor) */
    private OutputAnchor fun;

    /** whether the internal anchor types are being refreshed*/
    private boolean internalRefresh;

    /**
     * Constructs a DefinitionBlock that is an untyped lambda of n arguments.
     * @param pane the parent ui pane.
     * @param arity the number of arguments of this lambda.
     */
    public DefinitionBlock(CustomUIPane pane, int arity) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.signature.setText("");
        this.signature.setVisible(false);
        this.args = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            this.args.add(new BinderAnchor(this, new Binder("x_" + i)));
        }
        this.res = new ResultAnchor(this, Optional.empty());
        this.internalRefresh = false;
        this.setupAnchors();
    }
            
    public DefinitionBlock(CustomUIPane pane, String name, Type type) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.args = new ArrayList<>();
        this.signature.setText(name + " :: " + type.prettyPrint());
        // Collect argument types and result type
        Type t = type;
        int i = 0;
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            args.add(new BinderAnchor(this, new Binder(name + "_x" + i, ft.getArgument())));
            i++;
            t = ft.getResult();
        }
        this.res = new ResultAnchor(this, Optional.of(t));

        this.setupAnchors();
    }

    private void setupAnchors() {
        this.argSpace.getChildren().addAll(this.args);
        this.resSpace.getChildren().add(this.res);
        this.fun = new OutputAnchor(this);
        this.funSpace.getChildren().add(this.fun);
        TactilePane.setGoToForegroundOnContact(this, false);
    }

    @Override
    public Optional <OutputAnchor> getOutputAnchor() {
        return Optional.of(this.fun);
    }

    @Override
    public void handleConnectionChanges() {
        if (! (this.exprIsDirty || this.internalRefresh)) {
            // refresh the types of the internal anchors first 
            TypeScope scope = new TypeScope();
            for (BinderAnchor arg : this.args) {
                arg.refreshAnchorType(scope);
            }
            this.res.refreshAnchorType(scope);

            // propagate into the internal blocks, but avoid self recursion
            this.internalRefresh = true;
            if (this.res.hasConnection()) {
                this.res.getConnection(0).ifPresent(c -> c.handleConnectionChangesFrom(this.res));
            }
            // also propagate in from above in case the lambda is partially connected 
            for (BinderAnchor arg : this.args) {
                for (Optional<InputAnchor> anchor : arg.getOppositeAnchors()) {
                    if (anchor.isPresent()) {
                        InputAnchor fromInput = anchor.get();
                        fromInput.handleConnectionChanges();
                        // force early type checking of connection to binder anchor
                        fromInput.getConnection(0).get().handleConnectionChangesFrom(fromInput);
                    }
                }
            }
            this.internalRefresh = false;

            // continue as normal with propagating changes on the outside
            super.handleConnectionChanges();
        }
    }
    
    @Override
    public final void updateExpr() {
        List<Binder> binders = this.args.stream().map(arg -> arg.binder).collect(Collectors.toList());
        this.expr = new Lambda(binders, this.res.getExpr());
    }

    @Override
    public void refreshAnchorTypes() {
        // refresh the type of outside function anchor, internal anchor are refreshed earlier
        ArrayList<Type> types = new ArrayList<>();
        for (BinderAnchor arg : this.args) {
            types.add(arg.getType());
        }
        types.add(this.res.getType());

        this.fun.setType(Type.fun(types.toArray(new Type[this.args.size()+1])).getFresh());
    }

    @Override
    public void invalidateVisualState() {
        // also update the internal blocks connected to the internal anchor 
        this.res.getOppositeAnchor().ifPresent(a -> a.invalidateVisualState());
    }
}
