package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.ToplevelPane;
import nl.utwente.viskell.ui.DragContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class FunApplyBlock extends Block {
    
    /** Function input anchor that can be dragged down for currying. */
    private class FunInputAnchor extends Pane implements ConnectionAnchor.Target {

        /** The connection anchor for this input argument. */
        private final InputAnchor anchor;
        
        /** The input type label of this anchor. */
        private final Label inputType;

        /** Label with function arrow when in curried state. */
        private final Label curryArrow;
        
        /** The draggable pane combining both type label and curry arrow */
        private final Pane typePane;
        
        /** Whether this input argument is in curried state. */
        private boolean curried;
        
        /** The drag handler for this. */
        private final DragContext dragContext;
        
        public FunInputAnchor() {
            this.curried = false;
            this.inputType = new Label(".....");
            this.inputType.setMinWidth(USE_PREF_SIZE);
            this.inputType.getStyleClass().add("inputType");
            this.curryArrow = new Label("->");
            this.curryArrow.setMinWidth(USE_PREF_SIZE);
            this.curryArrow.getStyleClass().add("curryArrow");
            this.curryArrow.setVisible(false);
            this.curryArrow.setMouseTransparent(true);
            this.typePane = new HBox(this.inputType, this.curryArrow) {
                @Override
                public void relocate(double x, double y) {
                    super.relocate(x, y);
                    FunInputAnchor.this.dragShift(y);
                }
            };
            this.typePane.setMinWidth(USE_PREF_SIZE);
            this.typePane.setPickOnBounds(false);
            this.anchor = new InputAnchor(FunApplyBlock.this);
            this.anchor.layoutXProperty().bind(this.inputType.widthProperty().divide(2));
            this.getChildren().addAll(this.anchor, this.typePane);
            this.setTranslateY(-9);
            this.setPickOnBounds(false);

            dragContext = new DragContext(this.typePane);
            dragContext.setDragInitAction(c -> {this.curried = false;});
            dragContext.setDragFinishAction(c -> {
                double height = this.inputType.getHeight();
                boolean mostlyDown = (this.typePane.getLayoutY() > height) && !this.anchor.hasConnection();
                double newY = mostlyDown ? 1.5*height : 0;
                this.typePane.relocate(0, newY);
                this.curried = mostlyDown;
                FunApplyBlock.this.dragShiftOuput(newY - height);
                FunApplyBlock.this.initiateConnectionChanges();
                if (curried && this.anchor.getWireInProgress() != null) {
                    this.anchor.getWireInProgress().remove();
                }
            });
        }

        @Override
        public ConnectionAnchor getAssociatedAnchor() {
            return this.anchor;
        }

        @Override
        public double computePrefHeight(double width) {
            double height = this.inputType.prefHeight(width);
            this.dragContext.setDragLimits(new BoundingBox(0, 0, 0, height*1.5));
            return height;
        }

        /** Shift related things down to some distance. */
        private void dragShift(double y) {
            double height = this.inputType.getHeight();
            this.anchor.setLayoutY(y);
            this.anchor.setOpacity(1 - (y/(height)));
            this.anchor.setVisible(y < height);
            this.curryArrow.setManaged(y > height || Iterables.getLast(FunApplyBlock.this.inputs) != this);
            this.curryArrow.setVisible(y > height);
            FunApplyBlock.this.dragShiftOuput(y - height);
        }

        /** Refresh visual information such as types */
        public void invalidateVisualState() {
            this.anchor.invalidateVisualState();
            boolean validConnection = this.anchor.hasValidConnection();
            this.setTranslateY(validConnection ? 0 : -9);
            this.inputType.setText(validConnection ? "zyxwv" : this.anchor.getStringType()); 
            this.typePane.setVisible(!validConnection);
        }
    }
    
    /** The information about the function. */
    private FunctionReference funRef;

    private List<FunInputAnchor> inputs;
    
    /** Text label for the output type */
    private final Label resTypeLabel;
    
    /** The uncurried output type */
    private Type resType;
    
    /** The result anchor of this function. */
    private final OutputAnchor output;

    /** The background for curriedType labels */
    private final Pane curriedOutput;
    
    /** The space containing anchors and type labels. */
    @FXML private Pane bodySpace;
    
    private final Pane inputSpace;
    
    public FunApplyBlock(FunctionReference funRef, ToplevelPane pane) {
        super(pane);
        this.loadFXML("FunApplyBlock");

        this.funRef = funRef;
        this.funRef.initializeBlock(this);
        ((HBox)this.bodySpace.getParent()).getChildren().add(0, funRef.asRegion());

        this.inputs = new ArrayList<>();
        this.output = new OutputAnchor(this, new Binder("res"));
        
        this.resTypeLabel = new Label("");
        this.resTypeLabel.setMinWidth(USE_PREF_SIZE);
        this.resTypeLabel.getStyleClass().add("resultType");
        VBox outputSpace = new VBox(this.resTypeLabel, this.output);
        outputSpace.setAlignment(Pos.CENTER);
        outputSpace.setPickOnBounds(false);
        
        Type t = funRef.refreshedType(funRef.requiredArguments(), new TypeScope());
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            FunInputAnchor ia = new FunInputAnchor();
            this.inputs.add(ia);
            t = ft.getResult();
        }
        Iterables.getLast(FunApplyBlock.this.inputs).curryArrow.setManaged(false);

        this.inputSpace = new HBox(5, this.inputs.toArray(new Node[this.inputs.size()]));
        this.inputSpace.setPickOnBounds(false);
        
        this.curriedOutput = new Pane() {
                @Override
                public double computePrefWidth(double height) {
                    return Math.max(inputSpace.prefWidth(height), outputSpace.getLayoutX()+resTypeLabel.prefWidth(height));
                }
                @Override
                public double computePrefHeight(double width) {
                    return resTypeLabel.prefHeight(width)*2;
                }
            };
        this.curriedOutput.setVisible(false);
        this.curriedOutput.getStyleClass().add("curriedOutput");
        this.curriedOutput.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, null, new Insets(-1))));
            
        this.bodySpace.getChildren().addAll(this.curriedOutput, this.inputSpace, outputSpace);
        outputSpace.layoutYProperty().bind(this.inputSpace.heightProperty());
        outputSpace.layoutXProperty().bind(Bindings.max(0, this.inputSpace.widthProperty().divide(2).subtract(resTypeLabel.widthProperty().divide(2))));
        outputSpace.setTranslateY(9);
        
        this.curriedOutput.translateYProperty().bind(outputSpace.translateYProperty());
    }

    /** Shift the output type/anchor down to some distance. */
    private void dragShiftOuput(double y) {
        double shift = Math.max(0, y);
        if (this.inputs.stream().map(i -> i.curried).filter(c -> c).count() == 0) { 
            this.output.getParent().setTranslateY(9 + shift);
            this.curriedOutput.setVisible(false);
            this.curriedOutput.setManaged(false);
        } else {
            this.curriedOutput.setManaged(true);
            this.curriedOutput.setVisible(true);
            this.curriedOutput.requestLayout();
        }
    }
    
    public FunctionReference getFunReference() {
        return this.funRef;
    }

    public void convertToOpenApply(ApplyAnchor apply) {
        this.funRef.deleteLinks();
        this.funRef = apply;
        apply.initializeBlock(this);
        ((HBox)this.bodySpace.getParent()).getChildren().set(0, funRef.asRegion());
        this.initiateConnectionChanges();
    }
    
    @Override
    public List<InputAnchor> getAllInputs() {
        List<InputAnchor> res = new ArrayList<>(); 
        this.inputs.stream().map(i -> i.anchor).collect(Collectors.toCollection(() -> res));
        this.funRef.getInputAnchor().ifPresent(fia -> res.add(0, fia));
        return res;
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of(this.output);
    }

    @Override
    public Optional<Block> getNewCopy() {
        if (this.inputs.stream().map(i -> i.curried).filter(c -> c).count() == 0) {
            return Optional.of(new FunApplyBlock(this.funRef.getNewCopy(), this.getToplevel()));
        }
        
        return Optional.empty();
    }
    
    @Override
    protected void refreshAnchorTypes() {
        TypeScope scope = new TypeScope();
        Type type = this.funRef.refreshedType(this.inputs.size(), scope);
        for (FunInputAnchor arg : this.inputs) {
            if (type instanceof FunType) {
                FunType ftype = (FunType)type;
                arg.anchor.setFreshRequiredType(ftype.getArgument(), scope);
                type = ftype.getResult();
            } else {
                new RuntimeException("too many arguments in this functionblock " + funRef.getName());
            }
        }
        this.resType = type.getFresh(scope);
        
        Type curriedType = this.resType;
        for (FunInputAnchor arg : Lists.reverse(this.inputs)) {
            if (arg.curried) {
                curriedType = new FunType (arg.anchor.getType(), curriedType);
            }
        }
        this.output.setExactRequiredType(curriedType);
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        Expression expr = this.funRef.getLocalExpr(outsideAnchors);
        List<Binder> curriedArgs = new ArrayList<>();
        
        for (FunInputAnchor arg : this.inputs) {
            if (arg.curried) {
                Binder ca = new Binder("ca");
                curriedArgs.add(ca);
                expr = new Apply(expr, new LocalVar(ca));
            } else {
                expr = new Apply(expr, arg.anchor.getLocalExpr(outsideAnchors));
            }
        }
        
        if (curriedArgs.isEmpty()) {
            return expr;
        } else {
            return new Lambda(curriedArgs, expr);
        }
    }

    @Override
    public void invalidateVisualState() {
        this.funRef.invalidateVisualState();
        
        this.resTypeLabel.setText(this.resType.prettyPrint());

        for (FunInputAnchor arg : this.inputs) {
        	arg.invalidateVisualState();
        }
    }

    @Override
    public boolean checkValidInCurrentContainer() {
        return this.funRef.isScopeCorrectIn(this.container) && super.checkValidInCurrentContainer(); 
    }
    
    @Override
    public void deleteAllLinks() {
        this.funRef.deleteLinks();
        super.deleteAllLinks();
    }
    
    @Override
    public boolean canAlterAnchors() {
        return this.funRef instanceof ApplyAnchor;
    }

    @Override
    public void alterAnchorCount(boolean isRemove) {
        if (this.funRef instanceof ApplyAnchor) {
            ApplyAnchor apply = (ApplyAnchor)this.funRef; 
            if (apply.hasConnection()) {
                if (isRemove && apply.getType().countArguments() <= this.inputs.size()) {
                    return;
                }
                if (!isRemove && apply.getType().countArguments() >= this.inputs.size()) {
                    return;
                }
            }
            
            if (isRemove) {
                FunInputAnchor removed = this.inputs.remove(this.inputs.size()-1);
                this.inputSpace.getChildren().remove(removed);
            } else {
                FunInputAnchor extra = new FunInputAnchor();
                this.inputs.add(extra);
                this.inputSpace.getChildren().add(extra);
            }
            this.initiateConnectionChanges();
        }
    }
    
    
    @Override
    public String toString() {
        return funRef.getName();
    }

    @Override
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of("name", funRef.getName(), "curriedArgs", this.inputs.stream().map(i -> i.curried).toArray());
    }
    
}
