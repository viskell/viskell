package nl.utwente.viskell.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.ui.ComponentLoader;

/**
 * A class that represents an input field inside a FunctionBlock.
 * This basically combines a label with an anchor to which an input can be connected.
 */
public class InputArgument extends Pane implements ComponentLoader {
    /** The label on which to display type information. */
    @FXML Label inputLabel;
    
    /** The property of the text to be displayed on the inputLabel. */
    private StringProperty inputText;
    
    /** The InputAnchor belonging to this InputArgument */
    private InputAnchor inputAnchor;
    
    /**
     * Constructs a new InputArgument.
     * @param block Block to which this InputArgument belongs.
     */
    public InputArgument(Block block) {
        this.inputText = new SimpleStringProperty("-");
        this.loadFXML("InputArgument");

        this.inputAnchor = new InputAnchor(block);
        this.inputAnchor.layoutXProperty().bind(this.inputLabel.widthProperty().divide(2));
        this.inputAnchor.errorStateProperty().addListener(this::checkError);
        
        // Vertically center the label
        this.inputLabel.layoutYProperty().bind(this.heightProperty().divide(2).subtract(this.inputLabel.heightProperty().divide(2)));
        
        this.setPrefHeight(ArgumentSpace.HEIGHT);
        this.getChildren().add(this.inputAnchor);
    }
    
    /** @return the InputText. */
    public String getInputText() {
        return inputText.get();
    }
    
    /** Sets the InputText. */
    public void setInputText(String text) {
        inputText.set(text);
    }
    
    /** @return The inputTextProperty. */
    public StringProperty inputTextProperty() {
        return inputText;
    }
    
    /** @return The InputAnchor belonging to this InputArgument. */
    public InputAnchor getInputAnchor() {
        return inputAnchor;
    }
    /** @return The Label that displays the input's type. */
    public Label getInputLabel() {
        return inputLabel;
    }
    
    /**
     * ChangeListener that will set the error state according to the error state property.
     */
    private void checkError(ObservableValue<? extends Boolean> value, Boolean oldValue, Boolean newValue) {
        ObservableList<String> styleClass = this.getStyleClass();
        styleClass.removeAll("error");
        if (newValue) {
            styleClass.add("error");
        }
    }
}
