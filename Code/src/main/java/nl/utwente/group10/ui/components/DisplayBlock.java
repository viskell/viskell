package nl.utwente.group10.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;

import java.io.IOException;

/**
 * DisplayBlock is an extension of Block that only provides a display of the input
 * it receives through it's inputAnchor.
 * The input will be rendered visually on the Block.
 * DisplayBlock can be empty and contain no value at all, the value can be altered at any time
 * by providing a different input source using a Connection.
 */
public class DisplayBlock extends Block {
	
	/** The input this Block is receiving.**/
	private String input;
	
	/**
	 * Creates a new instance of DisplayBlock.
	 * @return new DisplayBlock instance
	 * @throws IOException
	 */
    private static DisplayBlock newInstance() throws IOException {
        DisplayBlock block = FXMLLoader.load(DisplayBlock.class.getResource("/ui/DisplayBlock.fxml"), null, new TactileBuilderFactory());
        return block;
    }
    
    /**
     * Sets the input flowing into the DisplayBlock and refresh the display.
     * @param input
     */
    public void setInput(String inputValue) {
    	input = inputValue;
    	((Label) this.lookup("#label_value")).setText(input);
    }
 
    /**
     * Returns the input value this Block has, to avoid confusion
     * from other blocks who expect output from a block the method
     * has been named getOutput instead of getInput
     * @return outputValue
     */
    public String getOutput() {
    	return input;
    }
}