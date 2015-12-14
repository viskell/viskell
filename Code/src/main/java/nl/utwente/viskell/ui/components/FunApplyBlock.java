package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Apply;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.FunVar;
import nl.utwente.viskell.haskell.expr.Lambda;
import nl.utwente.viskell.haskell.expr.LocalVar;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.CustomUIPane;
import nl.utwente.viskell.ui.DragContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class FunApplyBlock extends Block {
    
    /** Function input anchor that can be dragged down for currying. */
    private class FunInputAnchor extends Pane {

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
            this.typePane = new HBox(this.inputType, this.curryArrow) {
                @Override
                public void relocate(double x, double y) {
                    super.relocate(x, y);
                    FunInputAnchor.this.dragShift(y);
                }
            };
            this.typePane.setMinWidth(USE_PREF_SIZE);
            this.anchor = new InputAnchor(FunApplyBlock.this);
            this.anchor.layoutXProperty().bind(this.inputType.widthProperty().divide(2));
            this.getChildren().addAll(this.anchor, this.typePane);
            this.setTranslateY(-9);

            dragContext = new DragContext(this.typePane);
            dragContext.setDragInitAction(c -> {this.curried = false;});
            dragContext.setDragFinishAction(c -> {
                double height = this.inputType.getHeight();
                boolean mostlyDown = this.typePane.getLayoutY() > height;
                double newY = mostlyDown ? 1.5*height : 0;
                this.typePane.relocate(0, newY);
                this.curried = mostlyDown;
                FunApplyBlock.this.dragShiftOuput(newY - height);
                FunApplyBlock.this.initiateConnectionChanges();
            });
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
            FunApplyBlock.this.dragShiftOuput(y - height);
        }

        /** Refresh visual information such as types */
        public void invalidateVisualState() {
        	this.setTranslateY(this.anchor.hasConnection() ? 0 : -9);
            this.inputType.setText(this.anchor.hasConnection() ? "zyxwv" : this.anchor.getStringType()); 
            this.curryArrow.setVisible(this.curried);
            this.inputType.setVisible(this.anchor.errorStateProperty().get() || ! this.anchor.hasConnection());
        }
    }
    
    /** The information about the function. */
    private FunctionInfo funInfo;

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
    
    /** The Label in which the information of the function is displayed. */
    @FXML private Label functionInfo;
    
    public FunApplyBlock(FunctionInfo funInfo, CustomUIPane pane) {
        super(pane);
        this.loadFXML("FunApplyBlock");

        this.funInfo = funInfo;
        this.functionInfo.setText(funInfo.getDisplayName());
        this.inputs = new ArrayList<>();
        this.output = new OutputAnchor(this, new Binder("res"));
        
        this.resTypeLabel = new Label("");
        this.resTypeLabel.setMinWidth(USE_PREF_SIZE);
        this.resTypeLabel.getStyleClass().add("resultType");
        VBox outputSpace = new VBox(this.resTypeLabel, this.output);
        outputSpace.setAlignment(Pos.CENTER);
        
        Type t = funInfo.getFreshSignature();
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            FunInputAnchor ia = new FunInputAnchor();
            this.inputs.add(ia);
            t = ft.getResult();
        }

        Pane inputSpace = new HBox(0, this.inputs.toArray(new Node[this.inputs.size()]));
        this.curriedOutput = new Pane() {
                @Override
                public double computePrefWidth(double height) {
                    return Math.max(inputSpace.prefWidth(height), outputSpace.getLayoutX()+resTypeLabel.prefWidth(height));
                }
            };
        this.curriedOutput.setVisible(false);
        this.curriedOutput.getStyleClass().add("curriedOutput");
        this.curriedOutput.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, null)));
            
        this.bodySpace.getChildren().addAll(this.curriedOutput, inputSpace, outputSpace);
        outputSpace.layoutYProperty().bind(inputSpace.heightProperty());
        outputSpace.layoutXProperty().bind(inputSpace.widthProperty().divide(2).subtract(resTypeLabel.widthProperty().divide(2)));
        outputSpace.setTranslateY(9);
        
        this.curriedOutput.prefHeightProperty().bind(inputSpace.heightProperty().multiply(2));
        this.curriedOutput.translateYProperty().bind(outputSpace.translateYProperty());
    }

    /** Shift the output type/anchor down to some distance. */
    private void dragShiftOuput(double y) {
        double shift = Math.max(0, y);
        if (this.inputs.stream().map(i -> i.curried).filter(c -> c).count() == 0) { 
            this.output.getParent().setTranslateY(9 + shift);
            this.curriedOutput.setVisible(false);
        } else {
            this.curriedOutput.setVisible(true);
            this.curriedOutput.requestLayout();
        }
    }
    
    @Override
    public List<InputAnchor> getAllInputs() {
        return this.inputs.stream().map(i -> i.anchor).collect(Collectors.toList());
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of(this.output);
    }

    @Override
    protected void refreshAnchorTypes() {
        Type type = this.funInfo.getFreshSignature();
        TypeScope scope = new TypeScope();
        for (FunInputAnchor arg : this.inputs) {
            if (type instanceof FunType) {
                FunType ftype = (FunType)type;
                arg.anchor.setFreshRequiredType(ftype.getArgument(), scope);
                type = ftype.getResult();
            } else {
                new RuntimeException("too many arguments in this functionblock " + funInfo.getName());
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
    public Pair<Expression, Set<OutputAnchor>> getLocalExpr() {
        Expression expr = new FunVar(this.funInfo);
        List<Binder> curriedArgs = new ArrayList<>();
        Set<OutputAnchor> outsideAnchors = new HashSet<>();
        
        for (FunInputAnchor arg : this.inputs) {
            if (arg.curried) {
                Binder ca = new Binder("ca");
                curriedArgs.add(ca);
                expr = new Apply(expr, new LocalVar(ca));
            } else {
                Pair<Expression, Set<OutputAnchor>> pair = arg.anchor.getLocalExpr();
                expr = new Apply(expr, pair.a);
                outsideAnchors.addAll(pair.b);
            }
        }
        
        outsideAnchors.addAll(funInfo.getRequiredBlocks().stream().flatMap(block -> block.getAllOutputs().stream()).collect(Collectors.toList()));
        
        if (curriedArgs.isEmpty()) {
            return new Pair<>(expr, outsideAnchors);
        } else {
            return new Pair<>(new Lambda(curriedArgs, expr), outsideAnchors);
        }
    }

    @Override
    public void invalidateVisualState() {
        this.resTypeLabel.setText(this.resType.prettyPrint());

        for (FunInputAnchor arg : this.inputs) {
        	arg.invalidateVisualState();
        }
    }

    @Override
    public String toString() {
        return funInfo.getName();
    }

    @Override
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of("name", funInfo.getName(), "curriedArgs", this.inputs.stream().map(i -> i.curried).toArray());
    }
    
}
