package nl.utwente.group10.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import nl.utwente.ewi.caes.tactilefx.fxml.TactileBuilderFactory;

import java.io.IOException;

/**
 * OutputBlock is an extension of Block that only provides a display of the input
 * it receives through it's inputAnchor.
 * The input will be rendered visually on the Block.
 * OutputBlock can be empty and contain no value at all, the value can be altered at any time
 * by providing a different input source using a Connection.
 */
public class OutputBlock extends Block {
	
	/** The input this Block is receiving.**/
	private String input;
	/** Determines if this Block serves output.**/
	private boolean output;
	
	/**
	 * Creates a new instance of OutputBlock.
	 * @return new OutputBlock instance
	 * @throws IOException
	 */
    private static OutputBlock newInstance() throws IOException {
        OutputBlock block = FXMLLoader.load(OutputBlock.class.getResource("/ui/OutputBlock.fxml"), null, new TactileBuilderFactory());
        ((Label) block.lookup("#label_value")).setText("Connect Input");
        return block;
    }
    
    /**
     * Creates a new OutputBlock instance, servesThroughput is a boolean value to determine
     * if the newly created instance should initially put input through as output in addition to
     * displaying the value.
     * @param servesThroughput
     * @return new OutputBlock instance
     * @throws IOException
     */
    public static OutputBlock newInstance(boolean servesThroughput) throws IOException {
    	OutputBlock outputBlock = OutputBlock.newInstance();
    	outputBlock.enableOutput(servesThroughput);
    	
    	return outputBlock;
    }
    
    /**
     * Sets the input flowing into the OutputBlock
     * @param input
     */
    public void setInput(String inputValue) {
    	input = inputValue;
    }
    
    /**
     * Method to enable/disable if this OutputBlock serves output
     * in addition to displaying it.
     * @param enabledState
     */
    public void enableOutput(boolean enabledState) {
    	output = enabledState;
    }
 
    /**
     * If output has been enabled the initial input value will be returned,
     * if output has been disabled a null value will be returned instead.
     * @return outputValue
     */
    public String getOutput() {
    	String returnval = null;
    	if (output) {
    		returnval = input;
    	}
    	return returnval;
    }
}