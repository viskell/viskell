package nl.utwente.group10.ui.components.blocks;

import com.google.common.collect.ImmutableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.expr.*;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import org.codehaus.plexus.util.cli.Arg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefinitionBlock extends Block implements InputBlock, OutputBlock, ComponentLoader {
    @Override
    public OutputAnchor getOutputAnchor() {
        return fun;
    }

    @Override
    public Type getOutputType() {
        return type;
    }

    @Override
    public Type getOutputType(Env env) {
        return type;
    }

    @Override
    public Type getOutputSignature() {
        return type;
    }

    @Override
    public Type getOutputSignature(Env env) {
        return type;
    }

    @Override
    public Type getInputSignature(InputAnchor input) {
        return resType;
    }

    @Override
    public Type getInputSignature(int index) {
        return resType;
    }

    @Override
    public Type getInputType(InputAnchor input) {
        return resType;
    }

    @Override
    public Type getInputType(int index) {
        return resType;
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of();
    }

    @Override
    public List<InputAnchor> getActiveInputs() {
        return ImmutableList.of();
    }

    private class ArgumentBlock extends Block implements OutputBlock {
        private Type type;
        private Function.FunctionArgument arg;
        private OutputAnchor anchor;

        public ArgumentBlock(DefinitionBlock parent, Type type) {
            super(parent.getPane());
            this.type = type;
            this.arg = new Function.FunctionArgument(type);

            anchor = new OutputAnchor(this, parent.getPane());
        }

        @Override
        public Expr asExpr() {
            return arg;
        }

        public Function.FunctionArgument getArgument() {
            return arg;
        }

        @Override
        public OutputAnchor getOutputAnchor() {
            return anchor;
        }

        @Override
        public Type getOutputType() {
            return type;
        }

        @Override
        public Type getOutputType(Env env) {
            return type;
        }

        @Override
        public Type getOutputSignature() {
            return type;
        }

        @Override
        public Type getOutputSignature(Env env) {
            return type;
        }
    }
    /** The Anchor that is used as input. */
    private List<InputAnchor> inputs;

    @FXML private Pane argSpace;
    @FXML private Pane resSpace;
    @FXML private Pane funSpace;

    @FXML private Label signature;

    private List<ArgumentBlock> args;

    private InputAnchor res;
    private OutputAnchor fun;

    private Type type;
    private Type resType;


    public DefinitionBlock(CustomUIPane pane, String name, Type type) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.args = new ArrayList<>();

        signature.setText(name + " :: " + type.toHaskellType());

        Type t = type;
        while (t instanceof FuncT) {
            FuncT ft = (FuncT) t;
            Type argType = ft.getArgs()[0];
            args.add(new ArgumentBlock(this, argType));

            t = ft.getArgs()[1];
        }

        this.type = type;
        this.resType = t;

        argSpace.getChildren().addAll(getArgumentAnchors());

        res = new InputAnchor(this, pane);
        resSpace.getChildren().add(res);

        fun = new OutputAnchor(this, pane);
        funSpace.getChildren().add(fun);
    }

    private List<OutputAnchor> getArgumentAnchors() {
        return this.args.stream().map(ArgumentBlock::getOutputAnchor).collect(Collectors.toList());
    }

    private List<Function.FunctionArgument> getArguments() {
        return this.args.stream().map(ArgumentBlock::getArgument).collect(Collectors.toList());
    }

    @Override
    public Expr asExpr() {
        return new Function(res.asExpr(), getArguments());
    }
}
