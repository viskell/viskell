package nl.utwente.viskell.ui.components;

import java.util.List;
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

/**
 * ValueBlock is an extension of Block that contains only a value and does not
 * accept input of any kind. A single output source will be generated in order
 * to connect a ValueBlock to another Block.
 * <p>
 * Extensions of ValueBlock should never accept inputs, if desired the class
 * Block should be extended instead.
 * </p>
 */
public class ValueBlock extends Block {
    /** The OutputAnchor of this ValueBlock. */
    protected OutputAnchor output;

    /** The space containing the output anchor. */
    @FXML protected Pane outputSpace;
    
    /** The label containing the constrained type of this block */
    @FXML protected Label valueType;

    /** The label for placing the value of this block. */
    @FXML protected Label value;

    /**
     * Construct a new ValueBlock.
     * @param pane The parent pane this Block resides on.
     */
    public ValueBlock(ToplevelPane pane, Type type, String value) {
        this("ValueBlock", pane, type);
        this.setValue(value);
    }
    
    protected ValueBlock(String fxml, ToplevelPane pane, Type type) {
        super(pane);
        loadFXML(fxml);

        output = new OutputAnchor(this, new Binder("val", type));
        outputSpace.getChildren().add(output);
        outputSpace.setTranslateY(9);
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
    public Optional<Block> getNewCopy() {
        return Optional.of(new ValueBlock(this.getToplevel(), this.output.binder.getFreshAnnotationType(), this.value.getText()));
    }
    
    @Override
    public String toString() {
        return "ValueBlock[" + getValue() + "]";
    }
    
    @Override
    public void invalidateVisualState() {
        valueType.setText(output.getStringType());
    }

    @Override
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of("value", getValue());
    }
}
