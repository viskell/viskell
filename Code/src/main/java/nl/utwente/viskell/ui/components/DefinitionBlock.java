package nl.utwente.viskell.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.CustomUIPane;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

/**
 * A definition block is a block that represents a named lambda. It can be used to build lambda abstractions.
 */
public class DefinitionBlock extends Block implements ComponentLoader {

    /** the area in which the function anchor is in */
    @FXML private Pane funSpace;

    /* The label with the explicit name and type of this definition, if it has one. */
    @FXML private Label signature;

    /** The internal lambda within this definition block */
    private LambdaContainer body;
    
    /** The function anchor (second bottom anchor) */
    private OutputAnchor fun;

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
        
        this.body = new LambdaContainer(this, arity);
        ((VBox)this.getChildren().get(0)).getChildren().add(1, this.body);
        
        this.fun = new OutputAnchor(this, new Binder("lam"));
        this.funSpace.getChildren().add(this.fun);
        TactilePane.setGoToForegroundOnContact(this, false);
    }
            
    /**
     * Constructs a DefinitionBlock with an explicitly type function.
     * @param pane the parent ui pane.
     * @param name of the function.
     * @param type the full function type.
     */
    public DefinitionBlock(CustomUIPane pane, String name, Type type) {
        super(pane);
        this.loadFXML("DefinitionBlock");

        this.signature.setText(name + " :: " + type.prettyPrint());

        this.body = new LambdaContainer(this, name, type);
        ((VBox)this.getChildren().get(0)).getChildren().add(1, this.body);
        
        this.fun = new OutputAnchor(this, new Binder("lam"));
        this.funSpace.getChildren().add(this.fun);
        TactilePane.setGoToForegroundOnContact(this, false);
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of();
    }

    @Override
    public Optional <OutputAnchor> getOutputAnchor() {
        return Optional.of(this.fun);
    }

    @Override
    public void refreshAnchorTypes() {
        // do typechecking internal connections first so that the lambda type is inferred
        this.body.handleConnectionChanges(false);

        this.fun.setExactRequiredType(this.body.getLambdaType().getFresh());
    }

    public void handleConnectionChanges(boolean finalPhase) {
        // first propagate into the internals
        this.body.handleConnectionChanges(finalPhase);

        // continue as normal with propagating changes on the outside
        super.handleConnectionChanges(finalPhase);
    }
    
    @Override
    public final Expression getLocalExpr() {
        return this.body.getLocalExpr();
    }

    @Override
    public void invalidateVisualState() {
        this.body.invalidateVisualState();
        // TODO update fun anchor when it gets a type label
    }

}
