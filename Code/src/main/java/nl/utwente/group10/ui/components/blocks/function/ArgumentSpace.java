package nl.utwente.group10.ui.components.blocks.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
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
public class ArgumentSpace extends HBox implements ComponentLoader {
    /** Horizontal space to keep between elements in ArgumentSpace. */
    public static final double H_GAP = 10;
    
    /** Vertical height of the ArgumentSpace. */
    public static final double HEIGHT = 50;
    
    public static final double KNOT_SNAP_MODIFIER = 0.3;
    
    /** The block to which this ArgumentSpace belongs. */
    private FunctionBlock block;
    
    /** The knot, used to control the knot index. */
    private Circle knot;
    
    /**
     * The index of the knot, all inputs with higher than the
     * knot are inactive, creating higher order functions.
     */
    private int knotIndex;
    
    /** List of input arguments. */
    private List<InputArgument> leftArguments;
    
    /** The argument containing the function's output, its return value. */
    private Label rightArgument;

    /** Number of input arguments. */
    private int inputs;
    
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
        this.block = block;
        this.inputs = inputCount;
        this.inputID = ConnectionCreationManager.INPUT_ID_NONE;
        leftArguments = new ArrayList<InputArgument>();
        knotIndex = inputCount;

        //Create and attach Labels for the (left) arguments.
        for (int i = 0; i < inputCount; i++) {
            InputArgument arg = new InputArgument(block); // TODO
            leftArguments.add(arg);            
        }

        knot = new Circle(10);

        //Mouse listeners
        knot.setOnMousePressed(event -> {knotPressed(ConnectionCreationManager.INPUT_ID_MOUSE); event.consume();});
        knot.setOnMouseDragged(event -> {knotMoved(event); event.consume();});
        knot.setOnMouseReleased(event -> {knotReleased(ConnectionCreationManager.INPUT_ID_MOUSE); event.consume();});
        
        //Touch listeners
        knot.setOnTouchPressed(event -> {knotPressed(event.getTouchPoint().getId()); event.consume();});
        knot.setOnTouchMoved(event -> {knotMoved(event); event.consume();});
        knot.setOnTouchReleased(event -> {knotReleased(event.getTouchPoint().getId()); event.consume();});

        rightArgument = new Label();
        rightArgument.getStyleClass().add("rightArgument");

        this.getChildren().addAll(leftArguments);
        this.getChildren().addAll(knot, rightArgument);
        
        this.setPrefHeight(HEIGHT);
        this.setSpacing(H_GAP);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("argumentSpace");

        updateArguments();
    }

    /**
     * To be called when the knot gets pressed.
     * @param inputID InputID of the input that pressed the knot.
     */
    private void knotPressed(int inputID) {
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
            updateKnotIndex();
            knot.setTranslateX(0);
            updateArguments();
        } else {
            //Ignore, different touch point;
        }
    }

    private void updateKnotIndex() {
        int result;
        double x = knot.getLayoutX() + knot.getTranslateX() - (knot.getRadius() / 2);

        if (x > rightArgument.getBoundsInParent().getMinX()) {
            result = knotIndex + 1;
        } else {
            int bowtieIndex = 0;

            for (InputArgument arg : leftArguments) {
                if (x > arg.getLayoutX()) {
                    bowtieIndex++;
                }
            }

            result = bowtieIndex;
        }
        setKnotIndex(result);
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
            double layoutX = knot.getLayoutX();
            double eventX = sceneToLocal(sceneX,0).getX();

            // Move the knot to the mouse/touch position
            knot.setTranslateX(eventX - layoutX);

            // Update the program to match the new touch position
            updateKnotIndex();
            updateArguments();

            // Do a layout pass
            layout();

            // Move the knot relative to its new layout position
            layoutX = knot.getLayoutX();
            knot.setTranslateX(eventX - layoutX);
        }
    }

    /**
     * Method to indicate that the position of the knot might have changed.
     * 
     * It repositions the arguments based on the current position of the knot.
     */
    public void updateArguments() {
        for (int i = 0; i < leftArguments.size(); i++) {
            leftArguments.get(i).setManaged(i < knotIndex);
            leftArguments.get(i).setVisible(i < knotIndex);
        }
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

    public List<InputAnchor> getInputAnchors() {
        return anchorsFor(leftArguments);
    }

    public List<InputAnchor> getActiveInputAnchors() {
        return anchorsFor(leftArguments.subList(0, knotIndex));
    }

    private List<InputAnchor> anchorsFor(Collection<InputArgument> args) {
        return args.stream().map(InputArgument::getInputAnchor).collect(Collectors.toList());
    }

    /**
     * Sets the knot index to the given index
     * @param index New knot index.
     */
    private void setKnotIndex(int index) {
        int clamped = Math.max(0, Math.min(inputs, index));
        if (clamped != knotIndex) {
            knotIndex = clamped;
            block.setConnectionState(ConnectionCreationManager.nextConnectionState());
        }
    }
}
