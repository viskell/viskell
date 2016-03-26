package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.DragContext;
import nl.utwente.viskell.ui.ToplevelPane;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class BinOpApplyBlock extends Block {

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
            this.anchor = new InputAnchor(BinOpApplyBlock.this);
            this.anchor.layoutXProperty().bind(this.inputType.widthProperty().divide(2));
            this.getChildren().addAll(this.anchor, this.typePane);
            this.setTranslateY(-9);
            this.setPickOnBounds(false);

            dragContext = new DragContext(this.typePane);
            dragContext.setDragInitAction(c -> {this.curried = false;});
            dragContext.setDragFinishAction(c -> {
                double height = this.inputType.getHeight();
                boolean mostlyDown = (this.typePane.getLayoutY() > height*1.5) && !this.anchor.hasConnection();
                double newY = mostlyDown ? 2.5*height : 0;
                this.typePane.relocate(0, newY);
                this.curried = mostlyDown;
                BinOpApplyBlock.this.dragShiftOuput(newY);
                BinOpApplyBlock.this.initiateConnectionChanges();
                if (this.curried && this.anchor.getWireInProgress() != null) {
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
            this.dragContext.setDragLimits(new BoundingBox(0, 0, 0, height*2.5));
            return height;
        }

        /** Shift related things down to some distance. */
        private void dragShift(double y) {
            double height = this.inputType.getHeight();
            this.anchor.setLayoutY(y);
            this.anchor.setOpacity(1 - (y/(height)));
            this.anchor.setVisible(y < height);
            this.curryArrow.setManaged(y > height*1 || this == BinOpApplyBlock.this.leftInput);
            this.curryArrow.setVisible(y > height*1);
            BinOpApplyBlock.this.dragShiftOuput(y - height*1.5);
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
    private FunctionInfo funInfo;

    private final FunInputAnchor leftInput;
    
    private final FunInputAnchor rightInput;
    
    private final Pane currySpacer;
    
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

    public BinOpApplyBlock(ToplevelPane pane, FunctionInfo funInfo) {
        super(pane);
        this.loadFXML("BinOpApplyBlock");

        this.funInfo = funInfo;
        String name = funInfo.getDisplayName();
        /* The Label in which the information of the function is displayed. */
        Label functionInfo = new Label(name.substring(1, name.length() - 1));
        functionInfo.getStyleClass().add("operator");
        Pane infoArea = new StackPane(functionInfo);
        infoArea.setMinHeight(36);
        infoArea.setMaxHeight(36);
        this.output = new OutputAnchor(this, new Binder("res"));

        this.leftInput = new FunInputAnchor();
        this.rightInput = new FunInputAnchor();
        this.rightInput.curryArrow.setManaged(false);
        
        Label arrowSpacer = new Label("->");
        arrowSpacer.getStyleClass().add("curryArrow");
        arrowSpacer.setVisible(false);
        Pane inputSpace = new HBox(0, this.leftInput, infoArea, arrowSpacer, this.rightInput);
        inputSpace.setPickOnBounds(false);
        
        this.currySpacer = new Pane();
        this.currySpacer.setPrefHeight(0);
        this.currySpacer.setVisible(false);
        
        this.resTypeLabel = new Label("");
        this.resTypeLabel.setMinWidth(USE_PREF_SIZE);
        this.resTypeLabel.getStyleClass().add("resultType");
        this.resTypeLabel.translateYProperty().bind(this.currySpacer.heightProperty());
        this.output.translateYProperty().bind(this.currySpacer.heightProperty());
        
        VBox outputSpace = new VBox(this.currySpacer, this.resTypeLabel, this.output) {
            @Override
            public double computePrefHeight(double width) {
                return currySpacer.prefHeight(width) + resTypeLabel.prefHeight(width)/2;
            }
        };
        outputSpace.setMinHeight(USE_PREF_SIZE);
        outputSpace.setMaxHeight(USE_PREF_SIZE);
        outputSpace.setAlignment(Pos.CENTER);
        outputSpace.setPickOnBounds(false);
        
        this.curriedOutput = new Pane() {
                @Override
                public double computePrefWidth(double heigth) {
                    return Math.max(inputSpace.prefWidth(heigth), outputSpace.getLayoutX()+resTypeLabel.prefWidth(heigth));
                }
                @Override
                public double computePrefHeight(double width) {
                    return currySpacer.getHeight()*4;
                }
            };
        this.curriedOutput.setVisible(false);
        this.curriedOutput.getStyleClass().add("curriedOutput");
        this.curriedOutput.setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, null, new Insets(-1))));
            
        this.bodySpace.getChildren().addAll(this.curriedOutput, inputSpace, outputSpace);
        outputSpace.layoutYProperty().bind(inputSpace.heightProperty());
        outputSpace.layoutXProperty().bind(Bindings.max(0, inputSpace.widthProperty().divide(2).subtract(resTypeLabel.widthProperty().divide(2))));
        
        this.curriedOutput.translateYProperty().bind(inputSpace.heightProperty());
    }

    /** @return a Map of class-specific properties of this Block. */
    @Override
    protected Map<String, Object> toBundleFragment() {
        return ImmutableMap.of(
                "curriedArgs", new boolean[]{this.leftInput.curried, this.rightInput.curried},
                "funInfo", this.funInfo.toBundleFragment());
    }

    /** return a new instance of this Block type deserializing class-specific properties used in constructor **/
    public static BinOpApplyBlock fromBundleFragment(ToplevelPane pane, Map<String,Object> bundleFragment) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<Boolean> curriedArgs = (ArrayList<Boolean>)bundleFragment.get("curriedArgs");
        Map<String, Object> funInfoBundle = (Map<String, Object>)bundleFragment.get("funInfo");
        FunctionInfo funInfo = FunctionInfo.fromBundleFragment(funInfoBundle);
        BinOpApplyBlock binOpApplyBlock = new BinOpApplyBlock(pane, funInfo);
        binOpApplyBlock.leftInput.curried = curriedArgs.get(0);
        binOpApplyBlock.rightInput.curried = curriedArgs.get(1);
        return binOpApplyBlock;
    }

    private void dragShiftOuput(double y) {
        double shift = Math.max(0, y);
        if (this.leftInput.curried || this.rightInput.curried) {
            this.curriedOutput.setVisible(true);
            this.curriedOutput.requestLayout();
        } else { 
            this.currySpacer.setPrefHeight(shift);
            this.curriedOutput.setVisible(false);
            this.curriedOutput.requestLayout();
        }
	}

    public FunctionInfo getFunInfo() {
        return this.funInfo;
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
    public Optional<Block> getNewCopy() {
        if (this.leftInput.curried || this.rightInput.curried) {
            return Optional.empty();
        }
        
        return Optional.of(new BinOpApplyBlock(this.getToplevel(), this.funInfo));
    }

    @Override
	protected void refreshAnchorTypes() {
        FunType ft1 = (FunType)funInfo.getFreshSignature();
        FunType ft2 = (FunType) ft1.getResult();
        Type al = ft1.getArgument();
        Type ar = ft2.getArgument();
        Type rt = ft2.getResult();
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
	public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        Expression expr = new FunVar(this.funInfo);
        List<Binder> curriedArgs = new ArrayList<>();
        
        if (this.leftInput.curried) {
            Binder ca = new Binder("ca");
            curriedArgs.add(ca);
            expr = new Apply(expr, new LocalVar(ca));
        } else {
            expr = new Apply(expr, this.leftInput.anchor.getLocalExpr(outsideAnchors));
        }

        if (this.rightInput.curried) {
            Binder ca = new Binder("ca");
            curriedArgs.add(ca);
            expr = new Apply(expr, new LocalVar(ca));
        } else {
            expr = new Apply(expr, this.rightInput.anchor.getLocalExpr(outsideAnchors));
        }

        if (curriedArgs.isEmpty()) {
            return expr;
        } else {
            return new Lambda(curriedArgs, expr);
        }

	}

	@Override
	public void invalidateVisualState() {
        this.resTypeLabel.setText(this.resType.prettyPrint());
        this.leftInput.invalidateVisualState();
        this.rightInput.invalidateVisualState();
        this.output.invalidateVisualState();
    }
	
    @Override
    public String toString() {
        return funInfo.getName();
    }
}
