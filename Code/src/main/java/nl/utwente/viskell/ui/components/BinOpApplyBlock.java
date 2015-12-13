package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

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

public class BinOpApplyBlock extends Block {

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
            this.anchor = new InputAnchor(BinOpApplyBlock.this);
            this.anchor.layoutXProperty().bind(this.inputType.widthProperty().divide(2));
            this.getChildren().addAll(this.anchor, this.typePane);

            dragContext = new DragContext(this.typePane);
            dragContext.setDragInitAction(c -> {this.curried = false;});
            dragContext.setDragFinishAction(c -> {
                double height = this.inputType.getHeight();
                boolean mostlyDown = this.typePane.getLayoutY() > height;
                double newY = mostlyDown ? 2*height : 0;
                this.typePane.relocate(0, newY);
                this.curried = mostlyDown;
                BinOpApplyBlock.this.dragShiftOuput(newY - height);
                BinOpApplyBlock.this.initiateConnectionChanges();
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
            BinOpApplyBlock.this.dragShiftOuput(y - height);
        }

    }
    
    /** The information about the function. */
    private FunctionInfo funInfo;

    private final FunInputAnchor leftInput;
    
    private final FunInputAnchor rightInput;
    
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

    public BinOpApplyBlock(FunctionInfo funInfo, CustomUIPane pane) {
        super(pane);
        this.loadFXML("BinOpApplyBlock");

        this.funInfo = funInfo;
        this.functionInfo.setText(funInfo.getDisplayName());
        this.output = new OutputAnchor(this, new Binder("res"));
        
        this.resTypeLabel = new Label("");
        this.resTypeLabel.setMinWidth(USE_PREF_SIZE);
        this.resTypeLabel.getStyleClass().add("resultType");
        VBox outputSpace = new VBox(this.resTypeLabel, this.output);
        outputSpace.setAlignment(Pos.CENTER);
        
        this.leftInput = new FunInputAnchor();
        this.rightInput = new FunInputAnchor();
        
        Pane inputSpace = new HBox(0, this.leftInput, this.functionInfo, this.rightInput);
        this.curriedOutput = new Pane() {
                @Override
                public double computePrefWidth(double heigth) {
                    return Math.max(inputSpace.prefWidth(heigth), outputSpace.getLayoutX()+resTypeLabel.prefWidth(heigth));
                }
            };
        this.curriedOutput.setVisible(false);
        this.curriedOutput.getStyleClass().add("curriedOutput");
        this.curriedOutput.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, null)));
            
        this.bodySpace.getChildren().addAll(this.curriedOutput, inputSpace, outputSpace);
        outputSpace.layoutYProperty().bind(inputSpace.heightProperty());
        outputSpace.layoutXProperty().bind(inputSpace.widthProperty().divide(2).subtract(resTypeLabel.widthProperty().divide(2)));
        
        this.curriedOutput.layoutYProperty().bind(inputSpace.heightProperty().divide(2));
        this.curriedOutput.prefHeightProperty().bind(inputSpace.heightProperty());
        this.curriedOutput.translateYProperty().bind(outputSpace.translateYProperty());
    }

	private void dragShiftOuput(double y) {
        double shift = Math.max(0, y);
        if (this.leftInput.curried || this.rightInput.curried) {
            this.curriedOutput.setVisible(true);
            this.curriedOutput.requestLayout();
        } else { 
            this.output.getParent().setTranslateY(shift);
            this.curriedOutput.setVisible(false);
        }
	}

	@Override
	public List<InputAnchor> getAllInputs() {
		return ImmutableList.of(this.leftInput.anchor, this.rightInput.anchor);
	}

	@Override
	public List<OutputAnchor> getAllOutputs() {
		return ImmutableList.of(this.output);
	}

	@Override
	protected void refreshAnchorTypes() {
        FunType ft1 = (FunType)funInfo.getFreshSignature();
        FunType ft2 = (FunType) ft1.getArgument();
        Type ar = ft1.getArgument();
        Type al = ft2.getArgument();
        Type rt = ft1.getResult();
        TypeScope scope = new TypeScope();
        
        this.leftInput.anchor.setFreshRequiredType(al, scope);
        this.rightInput.anchor.setFreshRequiredType(ar, scope);
        this.resType = rt.getFresh(scope);
        
        Type curriedType = this.resType;
        if (this.rightInput.curried) {
            curriedType = new FunType (this.rightInput.anchor.getType(), curriedType);
        }
        if (this.leftInput.curried) {
            curriedType = new FunType (this.leftInput.anchor.getType(), curriedType);
        }
        this.output.setExactRequiredType(curriedType);

	}

	@Override
	public Pair<Expression, Set<OutputAnchor>> getLocalExpr() {
        Expression expr = new FunVar(this.funInfo);
        List<Binder> curriedArgs = new ArrayList<>();
        Set<OutputAnchor> outsideAnchors = new HashSet<>();
        
        if (this.leftInput.curried) {
            Binder ca = new Binder("ca");
            curriedArgs.add(ca);
            expr = new Apply(expr, new LocalVar(ca));
        } else {
            Pair<Expression, Set<OutputAnchor>> pair = this.leftInput.anchor.getLocalExpr();
            expr = new Apply(expr, pair.a);
            outsideAnchors.addAll(pair.b);
        }

        if (this.rightInput.curried) {
            Binder ca = new Binder("ca");
            curriedArgs.add(ca);
            expr = new Apply(expr, new LocalVar(ca));
        } else {
            Pair<Expression, Set<OutputAnchor>> pair = this.rightInput.anchor.getLocalExpr();
            expr = new Apply(expr, pair.a);
            outsideAnchors.addAll(pair.b);
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

        this.leftInput.inputType.setText(this.leftInput.anchor.getStringType());
        this.leftInput.curryArrow.setVisible(this.leftInput.curried);
        this.leftInput.inputType.setVisible(this.leftInput.anchor.errorStateProperty().get() || ! this.leftInput.anchor.hasConnection());

        this.rightInput.inputType.setText(this.rightInput.anchor.getStringType());
        this.rightInput.curryArrow.setVisible(this.rightInput.curried);
        this.rightInput.inputType.setVisible(this.rightInput.anchor.errorStateProperty().get() || ! this.rightInput.anchor.hasConnection());

	}
	
    @Override
    public String toString() {
        return funInfo.getName();
    }

    @Override
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of("name", funInfo.getName(), "curriedArgs", new boolean[]{this.leftInput.curried, this.rightInput.curried});
    }

}
