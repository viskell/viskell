package nl.utwente.group10.ui.components.blocks;

import com.google.common.collect.ImmutableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.expr.*;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A definition block is a block that represents a named lambda. It can be used to build lambda abstractions.
 *
 * For now, it requires the complete type of the function to be known in advance.
 */
public class DefinitionBlock extends Block implements InputBlock, OutputBlock, ComponentLoader {
    @Override
    public OutputAnchor getOutputAnchor() {
        return fun;
    }

    @Override
    public Type getOutputSignature(Env env) {
        return type;
    }

    @Override
    public Type getInputSignature(InputAnchor input) {
        return resType;
    }

    /** A block for attaching the argument (top) anchors to. */
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

        /** Returns the FunctionArgument expression we built. */
        public Function.FunctionArgument getArgument() {
            return arg;
        }

        @Override
        public OutputAnchor getOutputAnchor() {
            return anchor;
        }

        @Override
        public Type getOutputSignature(Env env) {
            return type;
        }
    }

    @FXML private Pane argSpace;
    @FXML private Pane resSpace;
    @FXML private Pane funSpace;

    @FXML private Label signature;

    private List<ArgumentBlock> args;

    /** The result anchor (first bottom anchor) */
    private InputAnchor res;

    /** The function anchor (second bottom anchor) */
    private OutputAnchor fun;

    /** The complete type of the function (and the type of the function anchor). */
    private Type type;

    /** The type of the result of the function (the last part of the signature). */
    private Type resType;


    public DefinitionBlock(CustomUIPane pane, String name, Type type) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.args = new ArrayList<>();

        signature.setText(name + " :: " + type.toHaskellType());

        // Collect argument types and result type
        Type t = type;
        while (t instanceof FuncT) {
            FuncT ft = (FuncT) t;
            args.add(new ArgumentBlock(this, ft.getArgs()[0]));
            t = ft.getArgs()[1];
        }

        this.type = type;
        this.resType = t;

        argSpace.getChildren().addAll(getArgumentAnchors());

        res = new InputAnchor(this, pane);
        resSpace.getChildren().add(res);

        fun = new OutputAnchor(this, pane);
        funSpace.getChildren().add(fun);

        TactilePane.setGoToForegroundOnContact(this, false);
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
