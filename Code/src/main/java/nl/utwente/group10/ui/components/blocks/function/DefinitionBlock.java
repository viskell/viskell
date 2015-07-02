package nl.utwente.group10.ui.components.blocks.function;

import com.google.common.collect.ImmutableList;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.expr.*;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.input.InputBlock;
import nl.utwente.group10.ui.components.blocks.output.OutputBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefinitionBlock extends Block implements InputBlock, OutputBlock, ComponentLoader {
    @Override
    public OutputAnchor getOutputAnchor() {
        return fun;
    }

    private class ArgumentBlock extends Block implements OutputBlock {
        private Type type;
        private OutputAnchor anchor;

        public ArgumentBlock(DefinitionBlock parent, Type type) {
            super(parent.getPane());
            this.type = type;
            this.expr = new Function.FunctionArgument(type);

            anchor = new OutputAnchor(this);
        }

        public Function.FunctionArgument getArgument() {
            return (Function.FunctionArgument) getExpr();
        }

        @Override
        public OutputAnchor getOutputAnchor() {
            return anchor;
        }
    }

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

        res = new InputAnchor(this);
        resSpace.getChildren().add(res);

        fun = new OutputAnchor(this);
        funSpace.getChildren().add(fun);
    }

    private List<OutputAnchor> getArgumentAnchors() {
        return this.args.stream().map(ArgumentBlock::getOutputAnchor).collect(Collectors.toList());
    }

    private List<Function.FunctionArgument> getArguments() {
        return this.args.stream().map(ArgumentBlock::getArgument).collect(Collectors.toList());
    }


    @Override
    public final void updateExpr() {
        // getPane().removeExprToFunction(expr);
        expr = new Function(res.getExpr(), getArguments());
        // getPane().putExprToFunction(expr, this);
        
        super.updateExpr();
    }
}
