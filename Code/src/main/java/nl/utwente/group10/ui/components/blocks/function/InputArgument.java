package nl.utwente.group10.ui.components.blocks.function;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * A class that represents an input field inside a FunctionBlock.
 * This basically combines a label with an anchor to which an input can be connected.
 */
public class InputArgument extends Pane implements ComponentLoader{
    
    /** The label on which to display type information. */
    @FXML Label inputLabel;
    
    /** The property of the text to be displayed on the inputLabel. */
    private StringProperty inputText;
    
    /** The InputAnchor belonging to this InputArgument */
    private InputAnchor inputAnchor;
    
    /**
     * Constructs a new InputArgument.
     * @param block Block to which this InputArgument belongs.
     * @param signature Type signature as should be accepted by the InputAnchor.
     */
    public InputArgument(Block block, Type signature) {
        inputText = new SimpleStringProperty(signature.toHaskellType());
        this.loadFXML("InputArgument");
        
        inputAnchor = new InputAnchor(block, signature);
        inputAnchor.layoutXProperty().bind(inputLabel.widthProperty().divide(2));
        inputAnchor.isErrorProperty().addListener(this::checkError);

        // Vertically center the label
        inputLabel.layoutYProperty().bind(this.heightProperty().divide(2).subtract(inputLabel.heightProperty().divide(2)));
        
        this.setPrefHeight(ArgumentSpace.HEIGHT);
        this.getChildren().add(inputAnchor);
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
    
    private void checkError(ObservableValue<? extends Boolean> value, Boolean oldValue, Boolean newValue) {
        setError(newValue);
    }
    
    /**
     * Sets this InputArgument to a possible error state.
     * @param error Whether or not this InputArgument should be in error state.
     */
    public void setError(boolean error) {
        ObservableList<String> styleClass = this.getStyleClass();
        if (error) {
            styleClass.removeAll("error");
            styleClass.add("error");
        } else {
            styleClass.removeAll("error");
        }        
    }
}
