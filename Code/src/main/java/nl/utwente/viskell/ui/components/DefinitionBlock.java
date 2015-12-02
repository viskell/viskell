package nl.utwente.viskell.ui.components;

import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.CustomUIPane;
import nl.utwente.viskell.ui.DragContext;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * A definition block is a block that represents a named lambda. It can be used to build lambda abstractions.
 */
public class DefinitionBlock extends Block implements ComponentLoader {

    /** the area in which the function anchor is in */
    @FXML private Pane funSpace;

    /* The label with the explicit name and type of this definition, if it has one. */
    @FXML private Label signature;

    /** The internal lambda within this definition block */
    private LambdaContainer body;
    
    /** The function anchor (second bottom anchor) */
    private OutputAnchor fun;
    
    /** The draggable resizer in the bottom right corner */
    private Polygon resizer;

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
        
        this.body = new LambdaContainer(this, arity);
        ((VBox)this.getChildren().get(0)).getChildren().add(1, this.body);
        
        this.fun = new OutputAnchor(this, new Binder("lam"));
        this.funSpace.getChildren().add(this.fun);
        this.dragContext.setGoToForegroundOnContact(false);
        this.setupResizer();
    }
            
    /**
     * Constructs a DefinitionBlock with an explicitly type function.
     * @param pane the parent ui pane.
     * @param name of the function.
     * @param type the full function type.
     */
    public DefinitionBlock(CustomUIPane pane, String name, Type type) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.signature.setText(name + " :: " + type.prettyPrint());

        this.body = new LambdaContainer(this, name, type);
        ((VBox)this.getChildren().get(0)).getChildren().add(1, this.body);
        
        this.fun = new OutputAnchor(this, new Binder("lam"));
        this.funSpace.getChildren().add(this.fun);
        this.dragContext.setGoToForegroundOnContact(false);
        this.setupResizer();
    }

    /** Add and initializes a resizer element to this block */
    private void setupResizer() {
        resizer = new Polygon();
        resizer.getPoints().addAll(new Double[]{20.0, 20.0, 20.0, 0.0, 0.0, 20.0});
        resizer.setFill(Color.BLUE);

        resizer.setManaged(false);
        this.getChildren().add(resizer);
        resizer.relocate(400, 400);

        DragContext sizeDrag = new DragContext(resizer);
        sizeDrag.setDragLimits(new BoundingBox(200, 200, Integer.MAX_VALUE, Integer.MAX_VALUE));
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
    public void refreshAnchorTypes() {
        // do typechecking internal connections first so that the lambda type is inferred
        this.body.handleConnectionChanges(false);

        this.fun.setExactRequiredType(this.body.getLambdaType().getFresh());
    }

    public void handleConnectionChanges(boolean finalPhase) {
        // first propagate into the internals
        this.body.handleConnectionChanges(finalPhase);

        // continue as normal with propagating changes on the outside
        super.handleConnectionChanges(finalPhase);
    }
    
    @Override
    public final Expression getLocalExpr() {
        return this.body.getLocalExpr();
    }

    @Override
    public void invalidateVisualState() {
        this.body.invalidateVisualState();
        // TODO update fun anchor when it gets a type label
    }
    
    @Override
    public boolean belongsOnBottom() {
        return true;
    }

}
