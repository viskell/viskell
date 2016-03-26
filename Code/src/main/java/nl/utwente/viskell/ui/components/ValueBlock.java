package nl.utwente.viskell.ui.components;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ToplevelPane;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nl.utwente.viskell.ui.serialize.Bundleable;

/**
 * ValueBlock is an extension of Block that contains only a value and does not
 * accept input of any kind. A single output source will be generated in order
 * to connect a ValueBlock to another Block.
 * <p>
 * Extensions of ValueBlock should never accept inputs, if desired the class
 * Block should be extended instead.
 * </p>
 */
public abstract class ValueBlock extends Block implements ConnectionAnchor.Target {
    /** The OutputAnchor of this ValueBlock. */
    protected OutputAnchor output;

    /** The space containing the output anchor. */
    @FXML protected Pane outputSpace;
    
    /** The label containing the constrained type of this block */
    @FXML protected Label valueType;

    /** The label for placing the value of this block. */
    @FXML protected Label value;

    protected Type type;

    /**
     * Construct a new ValueBlock.
     * @param pane The parent pane this Block resides on.
     */
    protected ValueBlock(String fxml, ToplevelPane pane, Type type) {
        super(pane);
        loadFXML(fxml);

        this.type = type;
        output = new OutputAnchor(this, new Binder("val", type));
        outputSpace.getChildren().add(output);
    }

    /**
     * @param newValue The value of this block to be used as output.
     */
    public final void setValue(String newValue) {
        value.setText(newValue);
    }

    /**
     * @return output The value that is outputted by this Block.
     */
    public final String getValue() {
        return value.getText();
    }

    public Type getAnnotationType() {
        return this.output.binder.getFreshAnnotationType();
    }
    
    @Override
    public ConnectionAnchor getAssociatedAnchor() {
        return output;
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        return new Value(output.getType(Optional.empty()), getValue());
    }

    @Override
    public void refreshAnchorTypes() {
        output.refreshType(new TypeScope());
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of();
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of(output);
    }
    
    @Override
    public String toString() {
        return "ValueBlock[" + getValue() + "]";
    }
    
    @Override
    public void invalidateVisualState() {
        valueType.setText(output.getStringType());
        this.output.invalidateVisualState();
    }
}
