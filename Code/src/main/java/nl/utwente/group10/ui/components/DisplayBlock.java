package nl.utwente.group10.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

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
    private StringProperty input;
    
    public DisplayBlock() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/DisplayBlock.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
    
        input = new SimpleStringProperty("New Output");
        
        fxmlLoader.load();
    }

    /**
     * Creates a new instance of DisplayBlock.
     * @return new DisplayBlock instance
     * @throws IOException
     */
    public static DisplayBlock newInstance() throws IOException {
        return new DisplayBlock();
    }

    /**
     * Sets the input flowing into the DisplayBlock and refresh the display.
     * @param input
     */
    public void setInput(String inputValue) {
        input.set(inputValue);
    }

    /**
     * Returns the input value this Block has, to avoid confusion
     * from other blocks who expect output from a block the method
     * has been named getOutput instead of getInput
     * @return outputValue
     */
    public String getOutput() {
        return input.get();
    }
}
