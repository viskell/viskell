package nl.utwente.viskell.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.viskell.haskell.expr.Annotated;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Lambda;
import nl.utwente.viskell.haskell.expr.LocalVar;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.CustomUIPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A definition block is a block that represents a named lambda. It can be used to build lambda abstractions.
 *
 * For now, it requires the complete type of the function to be known in advance.
 */
public class DefinitionBlock extends Block implements ComponentLoader {

    /** A block for attaching the argument (top) anchors to. */
    private class ArgumentBlock extends Block {
        /** The variable binder corresponding to this input argument */
        private Binder binder;
        private OutputAnchor anchor;

        public ArgumentBlock(DefinitionBlock parent, Binder binder) {
            super(parent.getPane());
            this.binder = binder;
            this.expr = new LocalVar(binder);

            anchor = new OutputAnchor(this);
        }

        @Override
        public Optional<OutputAnchor> getOutputAnchor() {
            return Optional.of(anchor);
        }

        @Override
        public void updateExpr() {
            this.expr = new LocalVar(this.binder);
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

    /** The type of the result of the function (the last part of the signature). */
    private Type resType;

    public DefinitionBlock(CustomUIPane pane, String name, Type type) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.args = new ArrayList<>();

        signature.setText(name + " :: " + type.prettyPrint());

        // Collect argument types and result type
        Type t = type;
        int i = 0;
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            args.add(new ArgumentBlock(this, new Binder(name + "_x" + i, ft.getArgument())));
            i++;
            t = ft.getResult();
        }

        this.resType = t;

        argSpace.getChildren().addAll(getArgumentAnchors());

        res = new InputAnchor(this);
        resSpace.getChildren().add(res);

        fun = new OutputAnchor(this);
        funSpace.getChildren().add(fun);

        TactilePane.setGoToForegroundOnContact(this, false);
    }

    @Override
    public Optional <OutputAnchor> getOutputAnchor() {
        return Optional.of(fun);
    }

    private List<OutputAnchor> getArgumentAnchors() {
        return this.args.stream().map(arg -> arg.getOutputAnchor().get()).collect(Collectors.toList());
    }

    @Override
    public final void updateExpr() {
        List<Binder> binders = this.args.stream().map(arg -> arg.binder).collect(Collectors.toList());
        Expression body = new Annotated(this.res.getExpr(), this.resType); 
        this.expr = new Lambda(binders, body);
    }
}
