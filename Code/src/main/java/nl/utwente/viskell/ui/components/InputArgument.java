package nl.utwente.viskell.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.ui.ComponentLoader;

/**
 * A class that represents an input field inside a FunctionBlock.
 * This basically combines a label with an anchor to which an input can be connected.
 */
public class InputArgument extends Pane implements ComponentLoader, ConnectionAnchor.Target {
    /** The label on which to display type information. */
    private Label inputLabel;
    
    /** The InputAnchor belonging to this InputArgument */
    private InputAnchor inputAnchor;
    
    /**
     * Constructs a new InputArgument.
     * @param block Block to which this InputArgument belongs.
     */
    public InputArgument(Block block) {
        this.inputLabel = new Label("-");
        this.getChildren().add(this.inputLabel);

        this.inputAnchor = new InputAnchor(block);
        this.inputAnchor.layoutXProperty().bind(this.inputLabel.widthProperty().divide(2));
        
        // Vertically center the label
        this.inputLabel.layoutYProperty().bind(this.heightProperty().divide(2).subtract(this.inputLabel.heightProperty().divide(2)));
        
        this.setPrefHeight(ArgumentSpace.HEIGHT);
        this.getChildren().add(this.inputAnchor);
        this.setPickOnBounds(false);
    }
    
    /** @return The InputAnchor belonging to this InputArgument. */
    public InputAnchor getInputAnchor() {
        return inputAnchor;
    }
    /** @return The Label that displays the input's type. */
    public Label getInputLabel() {
        return inputLabel;
    }

    @Override
    public ConnectionAnchor getAssociatedAnchor() {
        return inputAnchor;
    }

    /** Called when the VisualState changed. */
    public void invalidateVisualState() {
    	this.inputLabel.setText(this.inputAnchor.getStringType());
    	this.inputAnchor.invalidateVisualState();
    }

}
