package nl.utwente.group10.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;

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
	
	/**
	 * Creates a new ValueBlock instance with initialized value,
	 * once initialized value cannot be changed.
	 * @param value of this ValueBlock
	 * @return new ValueBlock instance
	 * @throws IOException
	 */
    public static ValueBlock newInstance(String value) throws IOException {
        ValueBlock block = FXMLLoader.load(ValueBlock.class.getResource("/ui/ValueBlock.fxml"), null, new TactileBuilderFactory());
        ((Label) block.lookup("#label_value")).setText(value);
        return block;
    }
}
