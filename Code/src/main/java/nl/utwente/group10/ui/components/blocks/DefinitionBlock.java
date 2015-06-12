package nl.utwente.group10.ui.components.blocks;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.expr.*;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.VarT;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;

import java.util.List;

public class DefinitionBlock extends Block implements OutputBlock, ComponentLoader {
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

    private Type type;

    private class ArgumentBlock extends Block {
        private Expr arg;

        public ArgumentBlock(CustomUIPane pane, Expr arg) {
            super(pane);
            this.arg = arg;
        }

        @Override
        public Expr asExpr() {
            return arg;
        }
    }
    /** The Anchor that is used as input. */
    private List<InputAnchor> inputs;

    @FXML private Pane argSpace;
    @FXML private Pane resSpace;
    @FXML private Pane funSpace;

    private InputAnchor res;
    private OutputAnchor fun;

    public DefinitionBlock(CustomUIPane pane) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        type = new FuncT(new ConstT("Float"), new ConstT("Float"));

        OutputAnchor arg = new OutputAnchor(new ArgumentBlock(pane, new Function.FunctionArgument(new VarT("a"))), pane);
        argSpace.getChildren().add(arg);

        res = new InputAnchor(this, pane);
        resSpace.getChildren().add(res);

        fun = new OutputAnchor(this, pane);
        funSpace.getChildren().add(fun);
    }

    @Override
    public Expr asExpr() {
        Function.FunctionArgument arg = new Function.FunctionArgument(new ConstT("Float"));

        Expr body = new Apply(new Ident("sin"), arg);

        return new Function(body, arg);
    }
}
