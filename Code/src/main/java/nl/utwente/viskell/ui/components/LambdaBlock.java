package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.Preferences;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.*;

/** Lambda block represent the externals of a lambda abstraction and can be named to become a local definition. */
public class LambdaBlock extends Block {

    /** the area in which the function anchor is in */
    @FXML private Pane funSpace;

    /** The label with the explicit name this definition, if it has one. */
    @FXML private Label definitionName;
    
    /** The label with the result type of this lambda. */
    @FXML private Label signature;

    /** The optional explicit type signature of this block */
    private Optional<Type> explicitSignature;
    
    /** The internal lambda within this definition block */
    private LambdaContainer body;
    
    /** The function anchor (second bottom anchor) */
    private PolyOutputAnchor fun;
    
    /** The draggable resizer in the bottom right corner */
    private Pane resizer;

    private List<LocalDefUse> allDefinitionUsers;
    
    /**
     * Constructs a DefinitionBlock that is an untyped lambda of n arguments.
     * @param pane the parent ui pane.
     * @param arity the number of arguments of this lambda.
     */
    public LambdaBlock(ToplevelPane pane, int arity) {
        super(pane);
        this.loadFXML("LambdaBlock");

        this.signature.setText("");
        this.explicitSignature = Optional.empty();
        this.definitionName.setText("");
        this.definitionName.setVisible(false);
        
        this.allDefinitionUsers = new ArrayList<>();
        
        this.body = new LambdaContainer(this, arity);
        ((VBox)this.getChildren().get(0)).getChildren().add(1, this.body);
        
        this.fun = new PolyOutputAnchor(this, new Binder("lam"));
        this.funSpace.getChildren().add(this.fun);
        this.dragContext.setGoToForegroundOnContact(false);

        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(new Double[]{20.0, 20.0, 20.0, 0.0, 0.0, 20.0});
        triangle.setFill(Color.BLUE);

        this.resizer = new Pane(triangle);
        triangle.setLayoutX(10);
        triangle.setLayoutY(10);
        this.resizer.setManaged(false);
        this.getChildren().add(resizer);
        this.resizer.relocate(300, 300);

        DragContext sizeDrag = new DragContext(this.resizer);
        sizeDrag.setDragLimits(new BoundingBox(200, 200, Integer.MAX_VALUE, Integer.MAX_VALUE));
        // make the width of the name/signature to lower limit for the resizer  
        ((Pane)this.signature.getParent()).widthProperty().addListener(e -> {
            double width = ((ReadOnlyDoubleProperty)e).doubleValue();
            if (width > 200) {
                sizeDrag.setDragLimits(new BoundingBox(width, 200, Integer.MAX_VALUE, Integer.MAX_VALUE));
            }
            if ((width-20) > this.resizer.getLayoutX()) {
                Platform.runLater(() -> this.resizer.relocate(width-20, this.resizer.getLayoutY()));
            }
        });
    }
    
    /** 
     * Construct a function block performing this block's actions
     * @param event the event triggering this creation
     */
    protected void createFunctionUseBlock(Event event) {
        ToplevelPane toplevel = this.getToplevel();
        LocalDefUse funRef = new LocalDefUse(this);
        this.allDefinitionUsers.add(funRef);
        Block block = Preferences.userNodeForPackage(Main.class).getBoolean("verticalCurry", true) ? new FunApplyBlock(funRef, toplevel) : new FunctionBlock(funRef, toplevel);
        toplevel.addBlock(block);
        Bounds bounds = this.getBoundsInParent();
        block.relocate(bounds.getMinX()-20, bounds.getMaxY()+10);
        block.refreshContainer();
        block.initiateConnectionChanges();
        event.consume();
    }
    
    public String getName() {
        return this.definitionName.getText();
    }
    
    /** @return whether this is an unnamed lambda */
    public boolean isTypedLambda() {
        return this.explicitSignature.isPresent();
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
        this.body.setPrefHeight(this.resizer.getBoundsInParent().getMaxY() - 25);
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
    public List<ConnectionAnchor> getAllAnchors() {
        List<ConnectionAnchor> result = new ArrayList<>(this.body.getAllAnchors());
        result.add(this.fun);
        return result;
    }

    @Override
    public Optional<Block> getNewCopy() {
        // copying the internals is too complex for now
        return Optional.empty();
    }

    public void removeUser(LocalDefUse user) {
        this.allDefinitionUsers.remove(user);
    }
    
    @Override
    public void refreshAnchorTypes() {
        // do typechecking internal connections first so that the lambda type is inferred
        body.handleConnectionChanges(false);

        fun.setExactRequiredType(explicitSignature.orElse(body.getLambdaType()).getFresh());
    }

    public void handleConnectionChanges(boolean finalPhase) {
        // first propagate into the internals
        this.body.handleConnectionChanges(finalPhase);

        // also users of this function block need to be updated
        for (LocalDefUse user : this.allDefinitionUsers) {
            user.handleConnectionChanges(finalPhase);
        }
        
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
        if (!this.explicitSignature.isPresent()) {
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
        for (LocalDefUse user : new ArrayList<>(this.allDefinitionUsers)) {
            user.onDefinitionRemoved();
        }
        
        this.body.deleteAllLinks();
        super.deleteAllLinks();
    }

    protected void shiftAndGrow(double shiftX, double shiftY, double extraX, double extraY) {
        super.relocate(this.getLayoutX() + shiftX , this.getLayoutY() + shiftY);
        this.resizer.relocate(this.resizer.getLayoutX() + extraX, this.resizer.getLayoutY() + extraY);
    }

    public void resizeToFitAll() {
        if (this.body.getAttachedBlocks().count() == 0) {
            // resize to default
            double diffX = this.resizer.getLayoutX() - 300;
            double diffY = this.resizer.getLayoutY() - 300;
            this.shiftAndGrow(diffX/2, diffY/2, -diffX, -diffY);
        } else {
            // resize to the union of all contained blocks, taking in account minimum size and some margins
            Bounds current = this.getToplevel().sceneToLocal(this.body.localToScene(this.body.getBoundsInLocal()));
            Bounds union = this.body.getAttachedBlocks().map(b -> b.getBoundsInParent()).reduce(BlockContainer::union).get();
            double shiftX = union.getMinX() - current.getMinX();
            double shiftY = union.getMinY() - current.getMinY();
            double extraX = union.getWidth() - current.getWidth();
            double extraY = union.getHeight() - current.getHeight();
            double marginX = Math.max(20, 200 - union.getWidth());
            double marginY = Math.max(20, 200 - union.getHeight());
            this.shiftAndGrow(shiftX - marginX/2 , shiftY - marginY/2 , extraX + marginX, extraY + marginY);
        }
    }
    
    @Override
    public boolean canAlterAnchors() {
        return !this.isTypedLambda();
    }
    
    @Override
    public void alterAnchorCount(boolean isRemove) {
        if (isRemove) {
            this.body.removeLastInput();
        } else {
            this.body.addExtraInput();
        }
    }
    
    public void editSignature() {
        String input = this.definitionName.getText().isEmpty() ? "example" : this.definitionName.getText();
        if (this.explicitSignature.isPresent()) {
            input += " :: " + this.explicitSignature.get().prettyPrint();
        }
                
        TextInputDialog dialog = new TextInputDialog(input);
        dialog.setTitle("Edit lambda signature");
        dialog.setHeaderText("Set the name and optionally the type");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(signature -> {
            if (! this.definitionName.isVisible()) {
                this.definitionName.addEventHandler(MouseEvent.MOUSE_RELEASED, this::createFunctionUseBlock);
                this.definitionName.addEventHandler(TouchEvent.TOUCH_RELEASED, this::createFunctionUseBlock);
            }
            
            List<String> parts = Splitter.on(" :: ").splitToList(signature);
            if (parts.size() < 2) {
                this.definitionName.setText(signature);
                this.definitionName.setVisible(true);
            } else {
                this.definitionName.setText(parts.get(0));
                this.definitionName.setVisible(true);
                // FIXME: what to do in case type parsing fail?
                Type type = this.getToplevel().getEnvInstance().buildType(parts.get(1));
                if (type.countArguments() >= this.body.argCount()) {
                    this.explicitSignature = Optional.of(type);
                    this.signature.setText(type.prettyPrint());
                    this.body.enforceExplicitType(type);
                    this.initiateConnectionChanges();
                }
            }
        });
    }

}
