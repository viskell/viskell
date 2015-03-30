package nl.utwente.group10.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Value;
import nl.utwente.group10.haskell.type.ConstT;

import java.io.IOException;

/**
 * ValueBlock is an extension of Block that contains only a value
 * and does not accept input of any kind.
 * A single output source will be generated in order to connect
 * a ValueBlock to another Block.
 * 
 * Extensions of ValueBlock should never accept inputs, if so desired
 * Block should be extended instead.
 */
public class ValueBlock extends Block {

    /** The value of this ValueBlock.*/
    private StringProperty value;
    
    @FXML private Pane outputSpace;

    /**
     * Creates a new ValueBlock instance with initialized value,
     * once initialized value cannot be changed.
     * @param value of this ValueBlock
     * @return new ValueBlock instance
     * @throws IOException
     */
    public ValueBlock(String val) throws IOException {
        super("ValueBlock", null);
        
        value = new SimpleStringProperty(val);
        
        this.getLoader().load();
        
        outputSpace.getChildren().add(this.getOutputAnchor());
    }

    /**
     * @param the value of this block to be used as output.
     */
    public void setValue(String StringVal) {
        this.value.set(StringVal);
    }

    /**
     * Returns the value that this block is outputting.
     * @return output
     */
    public String getValue() {
        return value.get();
    }
    
    /**
     * the StringProperty for the value of this ValueBlock.
     * @return value
     */
    public StringProperty valueProperty() {
        return value;
    }

    @Override
    public Expr asExpr() {
        // TODO: support more types than floats
        return new Value(new ConstT("Float"), getValue());
    }
}
