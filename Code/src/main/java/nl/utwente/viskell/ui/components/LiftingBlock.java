package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

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
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.ToplevelPane;

public class LiftingBlock extends Block {
    
    private NestedBlock nested;
    
    private List<LiftInputAnchor> inputs;
    
    /** Text label for the output type */
    private final Label resTypeLabel;

    /** The result anchor of this function. */
    private final OutputAnchor output;
    
    public LiftingBlock(ToplevelPane pane, NestedBlock nested) {
        super(pane);
        this.nested = nested;
        nested.setWrapper(this);
        List<Type> inputTypes = this.nested.getInputTypes();
        Type outputType = this.nested.getOutputTypes().get(0);
     
        this.inputs = new ArrayList<>();
        this.output = new OutputAnchor(this, new Binder("res"));
        this.resTypeLabel = new Label(outputType.prettyPrint());
        this.resTypeLabel.setMinWidth(USE_PREF_SIZE);
        this.resTypeLabel.getStyleClass().add("resultType");
     
        VBox outputSpace = new VBox(this.resTypeLabel, this.output);
        outputSpace.setAlignment(Pos.CENTER);
        outputSpace.setPickOnBounds(false);
        outputSpace.setTranslateY(9);

        for (@SuppressWarnings("unused") Type iType : inputTypes) {
            this.inputs.add(new LiftInputAnchor());
        }
        HBox inputSpace = new HBox(10, this.inputs.toArray(new Node[this.inputs.size()]));
        
        if (inputTypes.isEmpty()) {
            Pane dummySpace = new Pane();
            dummySpace.setPrefHeight(10);
            dummySpace.setVisible(false);
            inputSpace.getChildren().add(dummySpace);
        }
        
        inputSpace.setPickOnBounds(false);
        inputSpace.setAlignment(Pos.CENTER);
        
        VBox body = new VBox(inputSpace, nested, outputSpace);
        body.getStyleClass().addAll("block", "lifting");
        this.getChildren().add(body);
    }

    public NestedBlock getNested() {
        return nested;
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
        this.nested.refreshTypes();
        List<Type> inputTypes = this.nested.getInputTypes();
        Type outputType = this.nested.getOutputTypes().get(0);
        Type f = this.getToplevel().getEnvInstance().buildType(inputTypes.size() == 1 ? "Functor f => f" : "Applicative f => f");
        this.output.setExactRequiredType(Type.app(f, outputType));
        for (int i = 0; i < inputTypes.size(); i++) {
            this.inputs.get(i).anchor.setExactRequiredType(Type.app(f, inputTypes.get(i)));
        }
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        if (this.inputs.isEmpty()) {
            FunctionInfo pure = this.getToplevel().getEnvInstance().lookupFun("pure");
            return new Apply(new FunVar(pure), this.nested.getExpr());
        }
        
        FunctionInfo fmap = this.getToplevel().getEnvInstance().lookupFun("fmap");
        FunctionInfo ap = this.getToplevel().getEnvInstance().lookupFun("(<*>)");
        List<Expression> args = this.inputs.stream().map(input -> input.anchor.getLocalExpr(outsideAnchors)).collect(Collectors.toList());
        Expression expr = new Apply(new Apply(new FunVar(fmap), this.nested.getExpr()), args.get(0));
        for (int i = 1; i < args.size(); i++) {
            expr = new Apply(new Apply(new FunVar(ap), expr), args.get(i));
        }
        return expr;
    }

    @Override
    public void invalidateVisualState() {
        this.resTypeLabel.setText(this.output.getStringType());
        
        for (LiftInputAnchor input : this.inputs) {
            input.invalidateVisualState();
        }
        this.output.invalidateVisualState();
    }

    @Override
    public Optional<Block> getNewCopy() {
        return Optional.empty();
    }

    /** Combined input anchor and type label. */
    private class LiftInputAnchor extends VBox implements ConnectionAnchor.Target {

        /** The connection anchor for this input argument. */
        private final InputAnchor anchor;
        
        /** The input type label of this anchor. */
        private final Label inputType;

        private LiftInputAnchor() {
            this.inputType = new Label(".....");
            this.inputType.setMinWidth(USE_PREF_SIZE);
            this.inputType.getStyleClass().add("inputType");
            this.inputType.setAlignment(Pos.CENTER);
            this.anchor = new InputAnchor(LiftingBlock.this);
            this.anchor.setAlignment(Pos.CENTER);
            this.getChildren().addAll(this.anchor, this.inputType);
            this.setTranslateY(-9);
            this.setPickOnBounds(false);
            this.setAlignment(Pos.CENTER);
        }

        @Override
        public ConnectionAnchor getAssociatedAnchor() {
            return this.anchor;
        }

        /** Refresh visual information such as types */
        private void invalidateVisualState() {
            this.anchor.invalidateVisualState();
            boolean validConnection = this.anchor.hasValidConnection();
            this.setTranslateY(validConnection ? 0 : -9);
            this.inputType.setText(validConnection ? "zyxwv" : this.anchor.getStringType()); 
            this.inputType.setVisible(!validConnection);
        }
    }
    
}
