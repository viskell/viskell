package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Apply;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.FunVar;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.CustomUIPane;
import nl.utwente.viskell.ui.DragContext;

public class FunApplyBlock extends Block {
    
    /** Function input anchor that can be dragged down for currying. */
    private class FunInputAnchor extends Pane {

        /** The connection anchor for this input argument. */
        private final InputAnchor anchor;
        
        /** The draggable input type label. */
        private final Label inputType;

        /** Whether this input argument is in curried state. */
        private boolean curried;
        
        /** The drag handler for this. */
        private final DragContext dragContext;
        
        public FunInputAnchor() {
            this.curried = false;
            this.inputType = new Label(".....") {
                @Override
                public void relocate(double x, double y) {
                    super.relocate(x, y);
                    FunInputAnchor.this.dragShift(y);
                }
            };
            this.inputType.setMinWidth(USE_PREF_SIZE);
            this.inputType.getStyleClass().add("inputType");
            this.anchor = new InputAnchor(FunApplyBlock.this);
            this.anchor.layoutXProperty().bind(this.inputType.widthProperty().divide(2));
            this.getChildren().addAll(this.anchor, this.inputType);

            dragContext = new DragContext(this.inputType);
            dragContext.setDragInitAction(c -> {this.curried = false;});
            dragContext.setDragFinishAction(c -> {
                double height = this.inputType.getHeight();
                this.curried = this.inputType.getLayoutY() > height;
                this.inputType.relocate(0, this.curried ? 2*height : 0);
                FunApplyBlock.this.initiateConnectionChanges();
            });
        }

        @Override
        public double computePrefHeight(double width) {
            double height = this.inputType.prefHeight(width)*2;
            this.dragContext.setDragLimits(new BoundingBox(0, 0, 0, height));
            return height;
        }

        /** Shift related things down to some distance. */
        private void dragShift(double y) {
            double height = this.inputType.getHeight();
            this.anchor.setLayoutY(y);
            this.anchor.setOpacity(1 - (y/(height*1.5)));
            this.anchor.setVisible(y < height);
            FunApplyBlock.this.dragShiftOuput(y - height);
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

    /** The space containing anchors and type labels. */
    @FXML private Pane bodySpace;
    
    /** The Label in which the information of the function is displayed. */
    @FXML private Label functionInfo;
    
    public FunApplyBlock(FunctionInfo funInfo, CustomUIPane pane) {
        super(pane);
        this.loadFXML("FunApplyBlock");

        this.funInfo = funInfo;
        this.functionInfo.setText(funInfo.getName());
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

        Pane inputSpace = new HBox(10, this.inputs.toArray(new Node[this.inputs.size()]));
        this.bodySpace.getChildren().addAll(inputSpace, outputSpace);
        outputSpace.layoutYProperty().bind(inputSpace.heightProperty());
        outputSpace.layoutXProperty().bind(inputSpace.widthProperty().divide(2).subtract(resTypeLabel.widthProperty().divide(2)));
    }

    /** Shift the output type/anchor down to some distance. */
    private void dragShiftOuput(double y) {
        double shift = Math.max(0, y);
        if (this.inputs.stream().map(i -> i.curried).filter(c -> c).count() == 0) { 
            this.output.getParent().setTranslateY(shift);
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
    public Expression getLocalExpr() {
        Expression expr = new FunVar(this.funInfo);
        for (InputAnchor in : this.getAllInputs()) {
            expr = new Apply(expr, in.getLocalExpr());
        }
        return expr;
    }

    @Override
    public void invalidateVisualState() {
        this.resTypeLabel.setText(this.resType.prettyPrint());

        for (FunInputAnchor arg : this.inputs) {
            arg.inputType.setText(arg.anchor.getStringType());
            arg.inputType.setVisible(arg.anchor.errorStateProperty().get() || ! arg.anchor.hasConnection());
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
