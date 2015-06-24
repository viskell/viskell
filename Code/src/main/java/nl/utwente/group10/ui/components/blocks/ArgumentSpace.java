package nl.utwente.group10.ui.components.blocks;

import java.util.ArrayList;
import java.util.List;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.components.ComponentLoader;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
 *
 */
public class ArgumentSpace extends Pane implements ComponentLoader{    
    /**
     * Horizontal space to keep between elements in ArgumentSpace.
     */
    public static final double H_GAP = 10;
    
    /**
     * Vertical height of the ArgumentSpace.
     */
    public static final double HEIGHT = 50;
    
    /**
     * The block to which this ArgumentSpace belongs.
     */
    private FunctionBlock block;
    
    /**
     * The knot, used to control the knot index.
     */
    @FXML private Circle knot;
    
    /**
     * The index of the knot, all inputs with index higher or equal to the
     * knot are inactive, creating higher order functions.
     */
    private IntegerProperty knotIndex;
    
    /**
     * List of input arguments.
     */
    private List<InputArgument> leftArguments;
    
    /**
     * The argument containing the function's output.
     */
    @FXML private Label rightArgument;
    
    /**
     * InputID of the current action (INPUT_ID_NONE if idle)
     */
    private int inputID;
    
    /**
     * InputID that represents no current inputID.
     */
    public static final int INPUT_ID_NONE = -2;
    /**
     * InputID that represents the mouse.
     */
    public static final int INPUT_ID_MOUSE = -1;
    
    /**
     * Constructs an ArgumentSpace belonging to the function block as given.
     * @param block FunctionBlock to which this ArgumentSpace belongs.
     */
    public ArgumentSpace(FunctionBlock block, List<Type> inputSignatures) {
        this.loadFXML("ArgumentSpace");
        
        this.block = block;
        this.inputID = INPUT_ID_NONE;
        leftArguments = new ArrayList<InputArgument>();
        knotIndex = new SimpleIntegerProperty(0);
        //TODO knotIndex.addListener(event -> invalidateBowtieIndex());    
        
        //Create and attach Labels for the (left) arguments.
        for (int i = 0; i < inputSignatures.size(); i++) {
            InputArgument lbl = new InputArgument(block, inputSignatures.get(i));
            leftArguments.add(lbl);
            //centerLayoutVertical(lbl);
            
            if (i > 0) {
                Region prev = leftArguments.get(i-1);
                // The i-th (left)argument is placed to the right of argument i-1, with a horizontal space of H_GAP between them.
                lbl.layoutXProperty().bind(prev.layoutXProperty().add(prev.widthProperty()).add(H_GAP));
            }            
            this.getChildren().add(lbl);
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
        knot.setOnMousePressed(event -> {knotPressed(INPUT_ID_MOUSE); event.consume();});
        knot.setOnMouseDragged(event -> {knotMoved(event); event.consume();});
        knot.setOnMouseReleased(event -> {knotReleased(INPUT_ID_MOUSE); event.consume();});
        
        //Touch listeners
        knot.setOnTouchPressed(event -> {knotPressed(event.getTouchPoint().getId()); event.consume();});
        knot.setOnTouchMoved(event -> {knotMoved(event); event.consume();});
        knot.setOnTouchReleased(event -> {knotReleased(event.getTouchPoint().getId()); event.consume();});
        
        //Update the size of this Pane
        this.setPrefHeight(HEIGHT);
        this.setMaxHeight(USE_PREF_SIZE);
        this.prefWidthProperty().bind(getTotalWidthProperty());
        /*
         * TODO: PrefWidth is properly updating, yet the total visual space allocated to the ArgumentSpace is not.
         * Things attempted:
         * Setting min and max width besides pref width
         * Finding some sort of layout redraw method and call this on this's parent
         */
        
        //TODO invalidateArgumentContent();
        snapToKnotIndex();
    }
    
    /**
     * @return ObservableValue that represents the entire width of all the elements represented in this ArgumentSpace.
     */
    public ObservableValue<? extends Number> getTotalWidthProperty() {       
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
        if(this.inputID == INPUT_ID_NONE) {
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
            this.inputID = INPUT_ID_NONE;
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
            knotMoved(INPUT_ID_MOUSE, mEvent.getSceneX());
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
            knot.setLayoutX(Math.min(Math.max(localX, leftBound), rightBound));
            
            // Properly react on the change in the knot's position.
            setKnotIndex((int) Math.round(determineKnotIndex()));
            invalidateKnotPosition();
        }
    }
    
    /**
     * @return Determines the index of the knot, based on the knot's current position.
     * This index has a decimal part, this represents the percentage of the next argument that the knot has moved.
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
        String text = block.getOutputType().toHaskellType();
        rightArgument.setText(text);
        
    }
    
    /**
     * Method to indicate that the content in the input argument Labels are possibly outdated.
     */
    public void invalidateInputContent() {
        for (int i = 0; i < leftArguments.size(); i++) {
            setInputError(i, !block.inputTypeMatches(i));
            ((InputArgument) leftArguments.get(i)).setInputText(block.getInputType(i).toHaskellType());
        }
    }
    
    /**
     * Sets an input argument to visually display an error
     * @param index The argument's index
     * @param error Whether or not the argument is in error state.
     */
    public void setInputError(int index, boolean error) {
        ObservableList<String> styleClass = leftArguments.get(index).getStyleClass();
        if (error) {
            styleClass.removeAll("error");
            styleClass.add("error");
        } else {
            styleClass.removeAll("error");
        }
    }
    
    /**
     * @return The InputArguments this ArgumentSpace has.
     */
    public List<InputArgument> getInputArguments() {
        return leftArguments;
    }
    
    /**
     * @param index
     * @return The input argument with the index as specified.
     */
    public InputArgument getInputArgument(int index) {
        return leftArguments.get(index);
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
        knotIndex.set(index);        
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
