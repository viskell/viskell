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

            this.anchor = new OutputAnchor(this);
        }

        @Override
        public Optional<OutputAnchor> getOutputAnchor() {
            return Optional.of(this.anchor);
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

    /** The optional type of the result of the function (the last part of the signature). */
    private Optional<Type> resType;

    /**
     * Constructs a DefinitionBlock that is an untyped lambda of n arguments.
     * @param pane the parent ui pane.
     * @param arity the number of arguments of this lambda.
     */
    public DefinitionBlock(CustomUIPane pane, int arity) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.signature.setText("");
        this.signature.setVisible(false);
        this.args = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            this.args.add(new ArgumentBlock(this, new Binder("x_" + i)));
        }
        this.resType = Optional.empty();
        
        this.setupAnchors();
    }
            
    public DefinitionBlock(CustomUIPane pane, String name, Type type) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.args = new ArrayList<>();

        this.signature.setText(name + " :: " + type.prettyPrint());

        // Collect argument types and result type
        Type t = type;
        int i = 0;
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            args.add(new ArgumentBlock(this, new Binder(name + "_x" + i, ft.getArgument())));
            i++;
            t = ft.getResult();
        }

        this.resType = Optional.of(t);

        this.setupAnchors();
    }

    private void setupAnchors() {
        final List<OutputAnchor> argAnchors = this.args.stream().
                map(arg -> arg.getOutputAnchor().get()).collect(Collectors.toList());
        this.argSpace.getChildren().addAll(argAnchors);

        this.res = new InputAnchor(this);
        this.resSpace.getChildren().add(res);

        this.fun = new OutputAnchor(this);
        this.funSpace.getChildren().add(fun);

        TactilePane.setGoToForegroundOnContact(this, false);
    }

    @Override
    public Optional <OutputAnchor> getOutputAnchor() {
        return Optional.of(fun);
    }

    @Override
    public final void updateExpr() {
        List<Binder> binders = this.args.stream().map(arg -> arg.binder).collect(Collectors.toList());
        Expression body = this.res.getExpr();
        if (this.resType.isPresent()) {
            body = new Annotated(body, this.resType.get());
        }

        this.expr = new Lambda(binders, body);
    }
}
