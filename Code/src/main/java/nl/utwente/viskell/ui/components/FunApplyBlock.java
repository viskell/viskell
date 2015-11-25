package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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
    
    private static class FunResAnchor extends VBox {

        private final Label resType;
        private final OutputAnchor anchor;
        
        public FunResAnchor(Block block) {
            this.anchor = new OutputAnchor(block, new Binder("res"));
            this.resType = new Label(".....");
            this.resType.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
            this.resType.getStyleClass().add("resultType");
            this.setAlignment(Pos.CENTER);
            this.getChildren().addAll(this.resType, this.anchor);
        }
    }

    private static class FunInputAnchor extends Pane {

        private final InputAnchor anchor;
        
        private final Label inputType;

        private boolean curried;
        
        public FunInputAnchor(Block block, ChangeListener<Number> curryListener) {
            this.curried = false;
            this.anchor = new InputAnchor(block);
            this.inputType = new Label(".....") {
                @Override
                public void relocate(double x, double y) {
                    super.relocate(x, y);
                    FunInputAnchor.this.anchor.setLayoutY(y);
                    FunInputAnchor.this.anchor.setOpacity(1 - (y/this.getHeight()));
                    FunInputAnchor.this.anchor.setVisible(y < this.getHeight());
                }
            };
            this.inputType.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
            this.inputType.getStyleClass().add("inputType");
            this.anchor.setLayoutY(0);
            this.anchor.layoutXProperty().bind(this.inputType.widthProperty().divide(2));
            this.getChildren().addAll(this.anchor, this.inputType);
            this.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
            this.prefHeightProperty().bind(this.inputType.heightProperty().multiply(2));
            DragContext dc = new DragContext(this.inputType);
            dc.setDragInitAction(c -> {this.curried = false;});
            dc.setDragFinishAction(c -> {
                double inputY = this.inputType.getLayoutY();
                double height = this.inputType.getHeight();
                this.curried = inputY > height;
                this.inputType.relocate(0, this.curried ? 2*height : 0);
            });
            Platform.runLater(() -> {dc.setDragLimits(new BoundingBox(0, 0, 0, this.inputType.getHeight()*2));});
            this.inputType.layoutYProperty().subtract(this.inputType.heightProperty()).addListener(curryListener);
        }
    }
    
    /** The information about the function. */
    private FunctionInfo funInfo;

    private List<FunInputAnchor> inputs;
    
    /** The result anchor of this function. */
    private FunResAnchor output;
    
    /** The space containing the input anchor(s). */
    @FXML private Pane inputSpace;

    /** The space containing the output anchor. */
    @FXML private Pane outputSpace;

    /** The Label in which the information of the function is displayed. */
    @FXML private Label functionInfo;
    
    public FunApplyBlock(FunctionInfo funInfo, CustomUIPane pane) {
        super(pane);
        this.funInfo = funInfo;
        this.inputs = new ArrayList<>();
        
        this.loadFXML("FunApplyBlock");

        functionInfo.setText(funInfo.getName());
        
        output = new FunResAnchor(this);
        outputSpace.getChildren().add(output);
        
        ChangeListener<Number> curryListener = (observable, oldvalue, newValue) -> {
            double shift = Math.max(0, newValue.doubleValue());
            if (this.inputs.stream().map(i -> i.curried).filter(c -> c).count() == 0) { 
                this.output.setTranslateY(shift);
            }
        };
        
        
        Type t = funInfo.getFreshSignature();
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            FunInputAnchor ia = new FunInputAnchor(this, curryListener);
            this.inputs.add(ia);
            t = ft.getResult();
        }

        inputSpace.getChildren().addAll(this.inputs);
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return this.inputs.stream().map(i -> i.anchor).collect(Collectors.toList());
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of(output.anchor);
    }

    @Override
    protected void refreshAnchorTypes() {
        Type type = this.funInfo.getFreshSignature();
        TypeScope scope = new TypeScope();
        for (InputAnchor arg : this.getAllInputs()) {
            if (type instanceof FunType) {
                FunType ftype = (FunType)type;
                arg.setFreshRequiredType(ftype.getArgument(), scope);
                type = ftype.getResult();
            } else {
                new RuntimeException("too many arguments in this functionblock " + funInfo.getName());
            }
        }
        this.output.anchor.setFreshRequiredType(type, scope);
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
        this.output.resType.setText(this.output.anchor.getStringType());

        for (FunInputAnchor arg : this.inputs) {
            arg.inputType.setText(arg.anchor.getStringType());
            arg.inputType.setVisible(! arg.anchor.hasConnection());
        }
    }

}
