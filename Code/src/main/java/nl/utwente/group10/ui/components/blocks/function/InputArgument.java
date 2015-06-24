package nl.utwente.group10.ui.components.blocks.function;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class InputArgument extends Pane implements ComponentLoader{
    
    @FXML Label inputLabel;
    
    private StringProperty inputText;
    
    private InputAnchor inputAnchor;
    
    public InputArgument(Block block, Type signature) {
        inputText = new SimpleStringProperty(signature.toHaskellType());
        this.loadFXML("InputArgument");
        
        inputAnchor = new InputAnchor(block, signature);
        inputAnchor.layoutXProperty().bind(inputLabel.widthProperty().divide(2));
        inputAnchor.isErrorProperty().addListener(p -> setError(((BooleanProperty) p).get()));
        //TODO why is this cast suddenly necessary?

        inputLabel.layoutYProperty().bind(this.heightProperty().divide(2).subtract(inputLabel.heightProperty().divide(2)));
        
        this.setPrefHeight(ArgumentSpace.HEIGHT);
        this.getChildren().add(inputAnchor);
    }
    
    public String getInputText() {
        return inputText.get();
    }
    
    public void setInputText(String text) {
        inputText.set(text);
    }
    
    public StringProperty inputTextProperty() {
        return inputText;
    }
    
    public InputAnchor getInputAnchor() {
        return inputAnchor;
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
