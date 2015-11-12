package nl.utwente.viskell.ui.components;

import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.CustomUIPane;

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
    /** The value of this ValueBlock. */
    protected StringProperty value;

    /** The OutputAnchor of this ValueBlock. */
    protected OutputAnchor output;

    /** The space containing the output anchor. */
    @FXML protected BorderPane outputSpace;
    
    /** The label containing the constrained type of this block */
    @FXML protected Label valueType;

    /**
     * Construct a new ValueBlock.
     * @param pane
     *            The parent pane this Block resides on.
     */
    public ValueBlock(CustomUIPane pane, Type type, String value) {
        this(pane, type, value, "ValueBlock");
    }
    protected ValueBlock(CustomUIPane pane, Type type, String value, String fxml) {
        super(pane);

        this.value = new SimpleStringProperty(value);
        this.output = new OutputAnchor(this, new Binder("val", type));

        this.loadFXML(fxml);

        outputSpace.setCenter(this.getOutputAnchor().get());
        outputSpace.toFront();
    }

    /**
     * @param value
     *            The value of this block to be used as output.
     */
    public final void setValue(String value) {
        this.value.set(value);
    }

    /**
     * @return output The value that is outputted by this Block.
     */
    public final String getValue() {
        return value.get();
    }

    /** Returns the StringProperty for the value of this ValueBlock. */
    public final StringProperty valueProperty() {
        return value;
    }
    
    @Override
    public Expression getLocalExpr() {
        return new Value(this.output.getType(), getValue());
    }

    @Override
    public void refreshAnchorTypes() {
        this.output.refreshType(new TypeScope());
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of();
    }
    
    @Override
    public Optional<OutputAnchor> getOutputAnchor() {
        return Optional.of(output);
    }
    
    @Override
    public String toString() {
        return "ValueBlock[" + getValue() + "]";
    }
    
    @Override
    public void invalidateVisualState() {
        this.valueType.setText(this.output.getStringType());
    }

    @Override
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of("value", value.getValue());
    }
}
