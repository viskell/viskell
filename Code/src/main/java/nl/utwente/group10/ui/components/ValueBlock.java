package nl.utwente.group10.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

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
    }

    /**
     * Sets the value of this block so it can be used as output.
     * @param value
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
}
