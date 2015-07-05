package nl.utwente.group10.ui.components.blocks.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;

/**
 * Custom Pane that displays function's input and output and a knot to be able
 * to control the amount of inputs that are to be applied (amount of currying).
 * 
 * The input arguments are ordered using the layoutX, and the knotIndex is based
 * on this and the knot's layoutX. Usage of translateX is strictly for visual
 * improvements.
 * 
 * Supports multi-touch interaction.
 */
public class ArgumentSpace extends Pane implements ComponentLoader {    
    /** Horizontal space to keep between elements in ArgumentSpace. */
    public static final double H_GAP = 10;
    
    /** Vertical height of the ArgumentSpace. */
    public static final double HEIGHT = 50;
    
    public static final double KNOT_SNAP_MODIFIER = 0.3;
    
    /** The block to which this ArgumentSpace belongs. */
    private FunctionBlock block;
    
    /** The knot, used to control the knot index. */
    @FXML private Circle knot;
    
    /**
     * The index of the knot, all inputs with index higher or equal to the
     * knot are inactive, creating higher order functions.
     */
    private IntegerProperty knotIndex;
    
    /** List of input arguments. */
    private List<InputArgument> leftArguments;
    
    /** The argument containing the function's output, its return value. */
    @FXML private Label rightArgument;
    
    /** 
     * InputID of the current action (INPUT_ID_NONE if idle)
     * The inputID is associated with an input (INPUT_ID_MOUSE for mouse, > 0 for touch).
     * This is required to support multi-touch.
     */
    private int inputID;
    
    /**
     * Constructs an ArgumentSpace belonging to the function block as given.
     * @param block FunctionBlock to which this ArgumentSpace belongs.
     */
    public ArgumentSpace(FunctionBlock block, int inputCount) {
        this.loadFXML("ArgumentSpace");
        
        this.block = block;
        this.inputID = ConnectionCreationManager.INPUT_ID_NONE;
        leftArguments = new ArrayList<InputArgument>();
        knotIndex = new SimpleIntegerProperty(0);
        knotIndex.addListener(event -> snapToKnotIndex());    
        
        //Create and attach Labels for the (left) arguments.
        for (int i = 0; i < inputCount; i++) {
            InputArgument arg = new InputArgument(block);
            arg.getInputLabel().widthProperty().addListener(a -> Platform.runLater(block::updateLayout));
            leftArguments.add(arg);            
            if (i > 0) {
                Region prev = leftArguments.get(i-1);
                // The i-th (left)argument is placed to the right of argument i-1, with a horizontal space of H_GAP between them.
                arg.layoutXProperty().bind(prev.layoutXProperty().add(prev.widthProperty()).add(H_GAP));
            }            
            this.getChildren().add(arg);
        }
        
        //Put rightArgument's drawOrder on top 
        rightArgument.toFront();
        //Place rightArgument to the right of the knot, with a horizontal space of H_GAP between them.
        rightArgument.layoutXProperty().bind(knot.layoutXProperty().add(knot.radiusProperty()).add(H_GAP));
        centerLayoutVertical(rightArgument);
        
        //Put knot's drawOrder on to (above rightArgument)
        knot.toFront();
        //Vertically center the knot.
        knot.layoutYProperty().bind(this.heightProperty().divide(2));

        //Mouse listeners
        knot.setOnMousePressed(event -> {knotPressed(ConnectionCreationManager.INPUT_ID_MOUSE); event.consume();});
        knot.setOnMouseDragged(event -> {knotMoved(event); event.consume();});
        knot.setOnMouseReleased(event -> {knotReleased(ConnectionCreationManager.INPUT_ID_MOUSE); event.consume();});
        
        //Touch listeners
        knot.setOnTouchPressed(event -> {knotPressed(event.getTouchPoint().getId()); event.consume();});
        knot.setOnTouchMoved(event -> {knotMoved(event); event.consume();});
        knot.setOnTouchReleased(event -> {knotReleased(event.getTouchPoint().getId()); event.consume();});
        
        //Update the size of this Pane
        this.setPrefHeight(HEIGHT);
        this.setMaxHeight(USE_PREF_SIZE);
        this.setMinWidth(USE_PREF_SIZE);
        this.setMaxWidth(USE_PREF_SIZE);
        
        this.prefWidthProperty().bind(getTotalWidthProperty());
        rightArgument.widthProperty().addListener(a -> Platform.runLater(block::updateLayout));
        
        snapToKnotIndex();
    }
    
    /**
     * @return ObservableValue that represents the entire width of all the elements represented in this ArgumentSpace.
     */
    public DoubleBinding getTotalWidthProperty() {
        return rightArgument.layoutXProperty().add(rightArgument.translateXProperty()).add(rightArgument.widthProperty()).add(H_GAP);
    }
    
    /**
     * Binds a region's layoutY property to always be vertically centered in the ArgumentSpace. 
     * @param region
     */
    public void centerLayoutVertical(Region region) {
        region.layoutYProperty().bind(this.heightProperty().divide(2).subtract(region.heightProperty().divide(2)));
    }    

    /**
     * @return The knot's position used to determine the knot's index.
     */
    public double getKnotPos() {
        return knot.getLayoutX() - knot.getRadius();
    }
    
    /**
     * To be called when the knot gets pressed.
     * @param inputID InputID of the input that pressed the knot.
     */
    private void knotPressed(int inputID) {
        knot.layoutXProperty().unbind();
        if(this.inputID == ConnectionCreationManager.INPUT_ID_NONE) {
            this.inputID = inputID;
        } else {
            //Ignore, another touch point is already dragging;
        }
    }
    
    /**
     * To be called when the knot gets released
     * @param inputID InputID of the input that released the knot.
     */
    private void knotReleased(int inputID) {
        if(this.inputID == inputID) {
            this.inputID = ConnectionCreationManager.INPUT_ID_NONE;
            snapToKnotIndex();
        } else {
            //Ignore, different touch point;
        }
    }
    
    /**
     * To be called when the knot is moved
     * 
     * @param event
     *            InputEvent containing information about the input that is
     *            moving. Here an event instead of an ID is required, since
     *            getting the X and Y coordinate is necessary, and this
     *            requires separate actions for Mouse and Touch events.
     */
    private void knotMoved(InputEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent mEvent = (MouseEvent) event;
            knotMoved(ConnectionCreationManager.INPUT_ID_MOUSE, mEvent.getSceneX());
        } else if (event instanceof TouchEvent) {
            TouchEvent tEvent = (TouchEvent) event;
            knotMoved(tEvent.getTouchPoint().getId(), tEvent.getTouchPoint().getSceneX());
        }
    }
    
    /**
     * Helper method called when knotMoved() is called.
     * @param inputID InputID that moved the knot.
     * @param sceneX X coordinate (local to screen space) to where the knot is moved.
     */
    private void knotMoved(int inputID, double sceneX) {
        if(this.inputID == inputID) {
            // Transform X coordinate to local space.
            double localX = sceneToLocal(sceneX,0).getX();
            
            // Calculate knot's bounds and limit the knot's position to that
            double leftBound = 0 + knot.getRadius();
            Region leftMostArg = leftArguments.get(leftArguments.size() - 1);            
            double rightBound = leftMostArg.getLayoutX() + leftMostArg.getWidth() + H_GAP + knot.getRadius();

            //Set the knot's new position.
            if (knot.layoutXProperty().isBound()) {
                knot.layoutXProperty().unbind();
            }
            knot.setLayoutX(Math.min(Math.max(localX, leftBound), rightBound));
            
            // Properly react on the change in the knot's position.
            setKnotIndex((int) Math.round(determineKnotIndex() + KNOT_SNAP_MODIFIER));
            invalidateKnotPosition();
        }
    }
    
    /**
     * @return Determines the index of the knot, based on the knot's current
     *         position. This index has a decimal part, this represents the
     *         percentage of the next argument that the knot has moved.
     */
    private double determineKnotIndex() {
        if (Double.isNaN(getKnotPos())) {
            //This is needed for when the knot does not have a position yet (initializing).
            return getKnotIndex();
        }
        
        double bowtieIndex = 0;
        for (int i = 0; i < leftArguments.size(); i++) {
            Region argument = leftArguments.get(i);
            double min = argument.getLayoutX();
            double max = min + argument.getWidth();
            if (getKnotPos() > max) {
                bowtieIndex++;
            } else {
                bowtieIndex += Math.max(Math.min((getKnotPos() - min) / (max - min), 1), 0);
                break;
            }
        }
        return bowtieIndex;
    }

    /**
     * This repositions the knot to a position based on the current knot index,
     * regardless of the knot's current position.
     * 
     * Changes in Label length after snapping are accounted for by using
     * property bindings.
     */
    public void snapToKnotIndex() {
        if (inputID == ConnectionCreationManager.INPUT_ID_NONE) {
            int kti = getKnotIndex();
            if (kti > 0 && kti <= leftArguments.size()) {
                Region arg = leftArguments.get(kti-1); // First argument left of the knot.
    
                // Binds the knot to the first argument on the left, so that if
                // there is a length change to the left of the knot (because of a
                // Label's text change), the knot moves accordingly.
                knot.layoutXProperty().bind(arg.layoutXProperty().add(arg.widthProperty()).add(knot.radiusProperty()).add(H_GAP));
            }else if (kti == 0) { // There is no argument left of the knot.
                knot.setLayoutX(knot.getRadius());
            }
            
            invalidateKnotPosition();
        }
    }
    
    /**
     * Method to indicate that the position of the knot might have changed.
     * 
     * It repositions the arguments based on the current position of the knot.
     */
    public void invalidateKnotPosition() {
        double bti = determineKnotIndex();
        for (int i = 0; i < leftArguments.size(); i++) {
            Region argument = leftArguments.get(i);
            // Get only the percentage for this argument, clamped between 0 and 1, inverted.
            double percentage = Math.min(Math.max(bti - i,  0), 1);            
            percentage = Math.pow(percentage, 0.4); //Better looking curve
            
            //Updates the argument based on the percentage
            argument.setTranslateX((H_GAP +knot.getRadius() * 2) * (1 - percentage));
            argument.setOpacity(percentage);
            argument.setVisible(percentage > 0);
        }
    }
    
    /**
     * Method to indicate that the content in the argument Labels are possibly outdated.
     */
    public void invalidateArgumentContent() {
        invalidateOutputContent();
        invalidateInputContent();
    }
    
    /**
     * Method to indicate that the content in the output argument Label is possibly outdated.
     */
    public void invalidateOutputContent() {
        Optional<String> text = block.getOutputAnchor().getStringType();
        if (text.isPresent()) {
            rightArgument.setText(text.get());
        }
    }
    
    /**
     * Method to indicate that the content in the input argument Labels are possibly outdated.
     */
    public void invalidateInputContent() {
        for (InputArgument argument : leftArguments) {
            Optional<String> text = argument.getInputAnchor().getStringType();
            if (text.isPresent()) {
                argument.setInputText(text.get());
            }
        }
    }
    
    /**
     * @return The InputArguments this ArgumentSpace has.
     */
    public List<InputArgument> getInputArguments() {
        return leftArguments;
    }
    
    /**
     * @return The outputArgument.
     */
    public Region getOutputArgument() {
        return rightArgument;
    }

    /**
     * Sets the knot index to the given index
     * @param index New knot index.
     */
    public void setKnotIndex(int index) {
        if (index >= -1 && index <= getInputArguments().size()) {
            knotIndex.set(index); 
        } else {
            throw new IndexOutOfBoundsException();
        }       
    }
    
    /**
     * @return The knot's index.
     */
    public int getKnotIndex() {
        return knotIndex.get();        
    }
    
    /**
     * @return The knotIndex property
     */
    public IntegerProperty knotIndexProperty() {
        return knotIndex;
    }
}
