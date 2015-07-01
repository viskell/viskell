package nl.utwente.group10.ui.components.blocks.function;

import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A class that represents an input field inside a FunctionBlock.
 * This basically combines a label with an anchor to which an input can be connected.
 */
public class InputArgument extends BorderPane implements ComponentLoader{
    
    /** The label on which to display type information. */
    private Label inputLabel;
    
    /** The property of the text to be displayed on the inputLabel. */
    private StringProperty inputText;
    
    /** The InputAnchor belonging to this InputArgument */
    private InputAnchor inputAnchor;
    
    /** The ImageView used to indicate a type mismatch. */
    private ImageView errorImage;
    
    /**
     * Constructs a new InputArgument.
     * @param block Block to which this InputArgument belongs.
     */
    public InputArgument(Block block) {
        inputText = new SimpleStringProperty("");

        inputLabel = new Label();
        inputLabel.textProperty().bind(inputText);

        errorImage = new ImageView(new Image(this.getClass().getResourceAsStream("/ui/warningTriangle.png")));

        inputAnchor = new InputAnchor(block);
        inputAnchor.errorStateProperty().addListener(this::checkError);

        this.setTop(inputAnchor);
        BorderPane.setAlignment(inputAnchor, Pos.CENTER);
        this.setCenter(inputLabel);
    }
    
    /** @return the InputText. */
    public String getInputText() {
        return inputText.get();
    }
    
    /** Sets the InputText. */
    public void setInputText(String text) {
        inputText.set(text);
    }

    /** @return The InputAnchor belonging to this InputArgument. */
    public InputAnchor getInputAnchor() {
        return inputAnchor;
    }

    /**
     * ChangeListener that will set the error state according to the error state property.
     */
    private void checkError(ObservableValue<? extends Boolean> value, Boolean oldValue, Boolean newValue) {
        setError(newValue);
    }
    
    /**
     * Sets this InputArgument to a new error state.
     * @param error Whether or not this InputArgument should be in error state.
     */
    public void setError(boolean error) {
        ObservableList<String> styleClass = this.getStyleClass();
        if (error) {
            errorImage.setOpacity(1);
            styleClass.removeAll("error");
            styleClass.add("error");
        } else {
            errorImage.setOpacity(0);
            styleClass.removeAll("error");
        }        
    }
}
