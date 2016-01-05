package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.TouchContext;

/** Represent a lambda construct with internal anchor and blocks */
public class LambdaContainer extends BorderPane implements ComponentLoader, WrappedContainer {

    /* area containing argument anchors */
    @FXML private Pane argSpace;
    
    /* area contain result anchor */
    @FXML private Pane resSpace;

    /* The block this lambda is contained by. */
    private LambdaBlock wrapper;
    
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
    public LambdaContainer(LambdaBlock wrapper, int arity) {
        super();
        this.loadFXML("LambdaContainer");
        this.wrapper = wrapper;
        attachedBlocks = new HashSet<>();
        
        this.args = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            this.args.add(new BinderAnchor(this, wrapper, new Binder("a_" + i)));
        }
        this.res = new ResultAnchor(this, wrapper, Optional.empty());
        
        this.argSpace.getChildren().addAll(this.args);
        this.resSpace.getChildren().add(this.res);
        
        TouchContext context = new TouchContext(this, false);
        context.setPanningAction((deltaX, deltaY) -> {
            if (! this.wrapper.isActivated()) {
                this.wrapper.relocate(this.wrapper.getLayoutX() + deltaX, this.wrapper.getLayoutY() + deltaY);
            }
        });
    }
    
    /**
     * Set an explicit type requirement on this LambdaContainer.
     * @param signature the full function type.
     */
    protected void enforceExplicitType(Type signature) {
        Type constraint = signature.getFresh();
        constraint.enforcePolymorphism();
        Type t = constraint;
        for (BinderAnchor arg : this.args) {
            FunType ft = (FunType) t;
            arg.setExactRequiredType(ft.getArgument());
            t = ft.getResult(); 
        }
        
        this.res.setConstraintType(t);
    }

    public int argCount() {
        return this.args.size();
    }
    
    /** Adds extra input binder anchor to this lambda */
    public void addExtraInput() {
        BinderAnchor arg = new BinderAnchor(this, wrapper, new Binder("a_" + this.args.size()));
        this.args.add(arg);
        this.argSpace.getChildren().add(arg);
        this.wrapper.initiateConnectionChanges();
    }

    /** Removes the last input binder anchor of this lambda */
    public void removeLastInput() {
        if (this.args.size() > 1) {
            BinderAnchor arg = this.args.remove(this.args.size()-1);
            arg.removeConnections();
            this.argSpace.getChildren().remove(arg);
            this.wrapper.initiateConnectionChanges();
        }
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
            types.add(arg.getType(Optional.empty()));
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
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        Set<OutputAnchor> escapingAnchors = new HashSet<OutputAnchor>(); 
        
        List<Binder> binders = args.stream().map(arg -> arg.binder).collect(Collectors.toList());
        LetExpression body = new LetExpression(res.getLocalExpr(escapingAnchors), false);
        res.extendExprGraph(body, this, escapingAnchors);

        for (OutputAnchor anchor : escapingAnchors) {
            if (anchor.getContainer() == this) {
                anchor.extendExprGraph(body, this, outsideAnchors);
            } else {
                outsideAnchors.add(anchor);
            }
        }
        
        return new Lambda(binders, body);
    }

    /** Called when the VisualState changed. */
    public void invalidateVisualState() {
        for (BinderAnchor arg : this.args) {
            arg.invalidateVisualState();
        }
            
    	this.res.invalidateVisualState();
    }

    @Override
    public LambdaBlock getWrapper() {
        return this.wrapper;
    }
    
    public List<ConnectionAnchor> getAllAnchors() {
        List<ConnectionAnchor> result = new ArrayList<>(this.args);
        result.add(this.res);
        return result;
    }
    
    @Override
    public void attachBlock(Block block) {
        attachedBlocks.add(block);
    }

    @Override
    public void detachBlock(Block block) {
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
    public Node asNode() {
        return this;
    }
    
    @Override
    public void deleteAllLinks() {
       this.args.forEach(OutputAnchor::removeConnections);
       this.res.removeConnections();
       new ArrayList<>(this.attachedBlocks).forEach(block -> block.moveIntoContainer(this.getParentContainer()));
    }

    @Override
    public Bounds containmentBoundsInScene() {
        Bounds local = this.getBoundsInLocal();
        // include top and bottom border of the DefinitionBlock
        BoundingBox withBorders = new BoundingBox(local.getMinX(), local.getMinY()-25, local.getWidth(), local.getHeight()+50);
        return this.localToScene(withBorders);
    }

    @Override
    public void expandToFit(Bounds blockBounds) {
        Bounds containerBounds = this.wrapper.getToplevel().sceneToLocal(this.localToScene(this.getBoundsInLocal()));
        double shiftX = Math.min(0, blockBounds.getMinX() - containerBounds.getMinX());
        double shiftY = Math.min(0, blockBounds.getMinY() - containerBounds.getMinY());
        double extraX = Math.max(0, blockBounds.getMaxX() - containerBounds.getMaxX()) + Math.abs(shiftX);
        double extraY = Math.max(0, blockBounds.getMaxY() - containerBounds.getMaxY()) + Math.abs(shiftY);
        this.wrapper.shiftAndGrow(shiftX, shiftY, extraX, extraY);
        
        // also resize its parent in case of nested containers
        this.getParentContainer().expandToFit(this.wrapper.getBoundsInParent());
    }
}
