package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Lambda;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ComponentLoader;

/** Represent a lambda construct with internal anchor and blocks */
public class LambdaContainer extends BorderPane implements ComponentLoader, WrappedContainer {

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
    
    /** A set of blocks that belong to this container */
    protected Set<Block> attachedBlocks;

    /**
     * Constructs a LambdaContainer for an untyped lambda of n arguments.
     * @param arity the number of arguments of this lambda.
     */
    public LambdaContainer(DefinitionBlock wrapper, int arity) {
        super();
        this.loadFXML("LambdaContainer");
        this.wrapper = wrapper;
        attachedBlocks = new HashSet<>();
        
        this.args = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            this.args.add(new BinderAnchor(this, wrapper, new Binder("x_" + i)));
        }
        this.res = new ResultAnchor(this, wrapper, Optional.empty());
        
        this.argSpace.getChildren().addAll(this.args);
        this.resSpace.getChildren().add(this.res);
    }
    
    /**
     * Constructs a LambdaContainer with explicit types.
     * @param name of the function.
     * @param signature the full function type.
     */
    public LambdaContainer(DefinitionBlock wrapper, String name, Type signature) {
        super();
        this.loadFXML("LambdaContainer");
        this.wrapper = wrapper;
        attachedBlocks = new HashSet<>();
        
        // Make sure that the internal anchor typss stay as polymorphic as the signature
        Type constraint = signature.getFresh();
        constraint.enforcePolymorphism();

        // Collect argument types and result type
        this.args = new ArrayList<>();
        Type t = constraint;
        int i = 0;
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            args.add(new BinderAnchor(this, wrapper, new Binder(name + "_x" + i, ft.getArgument())));
            i++;
            t = ft.getResult();
        }
        this.res = new ResultAnchor(this, wrapper, Optional.of(t));

        this.argSpace.getChildren().addAll(this.args);
        this.resSpace.getChildren().add(this.res);
    }

    @Override
    public void refreshAnchorTypes() {
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

    @Override
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
    public Pair<Expression, Set<OutputAnchor>> getLocalExpr() {
        Pair<Expression, Set<OutputAnchor>> pair = res.getLocalExpr();
        List<Binder> binders = args.stream().map(arg -> arg.binder).collect(Collectors.toList());
        LetExpression body = new LetExpression(pair.a, false);
        Set<OutputAnchor> outsideAnchors = pair.b;
        res.extendExprGraph(body, this.wrapper.getPane().getTopLevel(), outsideAnchors);
        
        return new Pair<>(new Lambda(binders, body), outsideAnchors);
    }

    /** Called when the VisualState changed. */
    public void invalidateVisualState() {
        // TODO update anchors when they get a type label       
    }

    @Override
    public void attachBlock(Block block) {
        attachedBlocks.add(block);
    }

    @Override
    public void removeBlock(Block block) {
        attachedBlocks.remove(block);
    }
    
    @Override
    public Stream<Block> getAttachedBlocks() {
        return this.attachedBlocks.stream();
    }
    
    @Override
    public BlockContainer getParentContainer() {
        return wrapper.getContainer();
    }

    @Override
    public void deleteAllLinks() {
       this.args.forEach(OutputAnchor::removeConnections);
       this.res.removeConnections();
       this.attachedBlocks.forEach(block -> block.moveIntoContainer(this.wrapper.getPane().getTopLevel()));
    }

    @Override
    public Bounds getBoundsInScene() {
        return this.localToScene(this.getBoundsInLocal());
    }
}
