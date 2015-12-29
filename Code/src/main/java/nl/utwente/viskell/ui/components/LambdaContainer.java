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
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
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
            this.args.add(new BinderAnchor(this, wrapper, new Binder("" + (char)('a' + i))));
        }
        this.res = new ResultAnchor(this, wrapper, Optional.empty());
        
        this.argSpace.getChildren().addAll(this.args);
        this.resSpace.getChildren().add(this.res);
        
        this.setupHandlers();
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
        
        this.setupHandlers();
    }

    //** Setup event handlers in the container area for function menu */
    private void setupHandlers() {
    	this.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> event.consume());
    	this.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> event.consume());
    	this.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
    			if (event.getButton() != MouseButton.PRIMARY) {
    				Point2D menuPos = this.wrapper.getToplevel().screenToLocal(new Point2D(event.getScreenX(), event.getScreenY()));
    				this.wrapper.getToplevel().showFunctionMenuAt(menuPos.getX(), menuPos.getY(), true);
    			}
    		   	event.consume();
    		});
    	this.addEventHandler(TouchEvent.TOUCH_PRESSED, event -> event.consume());
    	this.addEventHandler(TouchEvent.TOUCH_MOVED, event -> event.consume());
    	this.addEventHandler(TouchEvent.TOUCH_RELEASED, event -> {
    			if (event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count() == 2) {
    				Point2D screenPos = new Point2D(event.getTouchPoint().getScreenX(), event.getTouchPoint().getScreenY());
    				Point2D menuPos = this.wrapper.getToplevel().screenToLocal(screenPos);
    				this.wrapper.getToplevel().showFunctionMenuAt(menuPos.getX(), menuPos.getY(), false);
    			}
    			event.consume();
    		});
    }
    
    /** Adds extra input binder anchor to this lambda */
    public void addExtraInput() {
        BinderAnchor arg = new BinderAnchor(this, wrapper, new Binder("x_" + this.args.size()));
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
        List<Binder> binders = args.stream().map(arg -> arg.binder).collect(Collectors.toList());
        LetExpression body = new LetExpression(res.getLocalExpr(outsideAnchors), false);
        res.extendExprGraph(body, this, outsideAnchors);
        
        return new Lambda(binders, body);
    }

    /** Called when the VisualState changed. */
    public void invalidateVisualState() {
        // TODO update anchors when they get a type label    
    	this.res.invalidateVisualState();
    }

    @Override
    public DefinitionBlock getWrapper() {
        return this.wrapper;
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
    public Bounds getBoundsInScene() {
        return this.localToScene(this.getBoundsInLocal());
    }

    @Override
    public void expandToFit(Bounds blockBounds) {
        Bounds containerBounds = this.wrapper.getToplevel().sceneToLocal(this.getBoundsInScene());
        double shiftX = Math.min(0, blockBounds.getMinX() - containerBounds.getMinX());
        double shiftY = Math.min(0, blockBounds.getMinY() - containerBounds.getMinY());
        double extraX = Math.max(0, blockBounds.getMaxX() - containerBounds.getMaxX()) + Math.abs(shiftX);
        double extraY = Math.max(0, blockBounds.getMaxY() - containerBounds.getMaxY()) + Math.abs(shiftY);
        this.wrapper.shiftAndGrow(shiftX, shiftY, extraX, extraY);
        
        // also resize its parent in case of nested containers
        this.getParentContainer().expandToFit(this.wrapper.getBoundsInParent());
    }
}
