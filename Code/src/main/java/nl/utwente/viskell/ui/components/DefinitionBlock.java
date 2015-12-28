package nl.utwente.viskell.ui.components;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.viskell.haskell.env.DefinitionFunction;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.ToplevelPane;
import nl.utwente.viskell.ui.DragContext;

import com.google.common.collect.ImmutableList;

/**
 * A definition block is a block that represents a named lambda. It can be used to build lambda abstractions.
 */
public class DefinitionBlock extends Block implements ComponentLoader {

    /** the area in which the function anchor is in */
    @FXML private Pane funSpace;

    /* The label with the explicit name and type of this definition, if it has one. */
    @FXML private Label signature;

    /** Whether the type signature was given */
    private boolean hasExplictiSignature;
    
    /** The internal lambda within this definition block */
    private LambdaContainer body;
    
    /** The function anchor (second bottom anchor) */
    private OutputAnchor fun;
    
    /** The draggable resizer in the bottom right corner */
    private Pane resizer;

    /** The function info corresponding to this block */
    protected Optional<DefinitionFunction> funInfo;

    /**
     * Constructs a DefinitionBlock that is an untyped lambda of n arguments.
     * @param pane the parent ui pane.
     * @param arity the number of arguments of this lambda.
     */
    public DefinitionBlock(ToplevelPane pane, int arity) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.signature.setText("");
        this.hasExplictiSignature = false;
        
        this.body = new LambdaContainer(this, arity);
        ((VBox)this.getChildren().get(0)).getChildren().add(1, this.body);
        
        this.fun = new OutputAnchor(this, new Binder("lam"));
        this.funSpace.getChildren().add(this.fun);
        this.dragContext.setGoToForegroundOnContact(false);
        this.setupResizer();
        
        funInfo = Optional.empty();
    }
            
    /**
     * Constructs a DefinitionBlock with an explicitly type function.
     * @param pane the parent ui pane.
     * @param name of the function.
     * @param type the full function type.
     */
    public DefinitionBlock(ToplevelPane pane, String name, Type type) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.signature.setText(name + " :: " + type.prettyPrint());
        this.hasExplictiSignature = true;

        this.body = new LambdaContainer(this, name, type);
        ((VBox)this.getChildren().get(0)).getChildren().add(1, this.body);
        
        this.fun = new OutputAnchor(this, new Binder("lam"));
        this.funSpace.getChildren().add(this.fun);
        this.dragContext.setGoToForegroundOnContact(false);
        this.setupResizer();
        
        funInfo = Optional.of(new DefinitionFunction(this, name, type));
        
        signature.addEventHandler(MouseEvent.MOUSE_RELEASED, this::createFunctionBlock);
    }

    /** Add and initializes a resizer element to this block */
    private void setupResizer() {
        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(new Double[]{20.0, 20.0, 20.0, 0.0, 0.0, 20.0});
        triangle.setFill(Color.BLUE);

        this.resizer = new Pane(triangle);
        triangle.setLayoutX(10);
        triangle.setLayoutY(10);
        this.resizer.setManaged(false);
        this.getChildren().add(resizer);
        this.resizer.relocate(400, 400);

        DragContext sizeDrag = new DragContext(this.resizer);
        sizeDrag.setDragLimits(new BoundingBox(200, 200, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    
    /** 
     * Construct a function block performing this block's actions
     * @param event the mouse event triggering this creation
     */
    protected void createFunctionBlock(MouseEvent event) {
        funInfo.ifPresent(info -> {
            Block block = (event.isControlDown()) ? new FunApplyBlock(info, getToplevel()) : new FunctionBlock(info, getToplevel());
            getToplevel().addBlock(block);
            Point2D pos = this.localToParent(0, 0);
            block.relocate(pos.getX(), pos.getY());
            block.initiateConnectionChanges();
            
            event.consume();
        });
    }
    
    /** @return whether this is an unnamed lambda */
    public boolean isLambda() {
        return !this.funInfo.isPresent();
    }
    
    /** @return The output binder of this block */
    public Binder getBinder() {
        return fun.binder;
    }
    
    @Override
    protected double computePrefWidth(double height) {
        this.body.setPrefWidth(this.resizer.getBoundsInParent().getMaxX());
        return super.computePrefWidth(height);
    }
    
    @Override 
    protected double computePrefHeight(double width) {
        this.body.setPrefHeight(this.resizer.getBoundsInParent().getMaxY() - this.signature.prefHeight(width));
        return super.computePrefHeight(width);
    }
    
    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of();
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of(this.fun);
    }

    @Override
    public Optional<Block> getNewCopy() {
        // copying the internals is too complex for now
        return Optional.empty();
    }

    @Override
    public void refreshAnchorTypes() {
        // do typechecking internal connections first so that the lambda type is inferred
        body.handleConnectionChanges(false);

        fun.setExactRequiredType(funInfo.map(FunctionInfo::getFreshSignature).orElse(body.getLambdaType().getFresh()));
    }

    public void handleConnectionChanges(boolean finalPhase) {
        // first propagate into the internals
        this.body.handleConnectionChanges(finalPhase);

        // continue as normal with propagating changes on the outside
        super.handleConnectionChanges(finalPhase);
    }
    
    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        return this.body.getLocalExpr(outsideAnchors);
    }

    @Override
    public void invalidateVisualState() {
        this.body.invalidateVisualState();
        if (!this.hasExplictiSignature) {
        	this.signature.setText(this.fun.getStringType());
        }

    }
    
    @Override
    public boolean belongsOnBottom() {
        return true;
    }
    
    public LambdaContainer getBody() {
        return body;
    }

    @Override
    public List<LambdaContainer> getInternalContainers() {
        return ImmutableList.of(this.body);
    }
    
    @Override
    public void relocate(double x, double y) {
        double dx = x-getLayoutX(), dy = y-getLayoutY();
        super.relocate(x, y);
        
        body.moveNodes(dx, dy);
    }

    @Override
    public void deleteAllLinks() {
        this.body.deleteAllLinks();
        super.deleteAllLinks();
    }

    protected void shiftAndGrow(double shiftX, double shiftY, double extraX, double extraY) {
        super.relocate(this.getLayoutX() + shiftX , this.getLayoutY() + shiftY);
        this.resizer.relocate(this.resizer.getLayoutX() + extraX, this.resizer.getLayoutY() + extraY);
    }

}
