package nl.utwente.viskell.ui.components;

import com.google.common.collect.Iterables;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.ConnectionCreationManager;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Modifies the snap range of the knot. Lower value results in a wider snap
     * range, effect is limited between -0.5 and 0.5.
     */
    public static final double KNOT_SNAP_MODIFIER = 0.3;

    /** The block to which this ArgumentSpace belongs. */
    private FunctionBlock block;

    /** The knot, used to control the knot index. */
    @FXML
    private Circle knot;

    /**
     * The index of the knot, all inputs with index higher or equal to the
     * knot are inactive, creating a function as output.
     */
    private IntegerProperty knotIndex;

    /** List of input arguments. */
    private List<InputArgument> leftArguments;

    /** The argument containing the function's output, its return value. */
    @FXML private Label rightArgument;

    /**
     * InputID of the current action (INPUT_ID_NONE if idle)
     * The inputID is associated with an input (INPUT_ID_MOUSE for mouse, > 0 for touch).
     * This is required to support multi-touch inputs.
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
        this.leftArguments = new ArrayList<InputArgument>();
        this.knotIndex = new SimpleIntegerProperty(0);

        this.knotIndex.addListener(event -> snapToKnotIndex());

        //Create and attach InputArguments for the inputs.
        for (int i = 0; i < inputCount; i++) {
            InputArgument arg = new InputArgument(block);
            // Make sure that when input widht changes, the total width also changes.
            arg.getInputLabel().widthProperty().addListener(a -> Platform.runLater(block::updateLayout));
            this.leftArguments.add(arg);

            if (i > 0) {
                Region prev = this.leftArguments.get(i-1);
                // The i-th (left)argument is placed to the right of argument i-1, with a horizontal space of H_GAP between them.
                arg.layoutXProperty().bind(prev.layoutXProperty().add(prev.widthProperty()).add(H_GAP));
            }
            this.getChildren().add(arg);
        }

        //Put rightArgument's drawOrder on top
        this.rightArgument.toFront();
        //Place rightArgument to the right of the knot, with a horizontal space of H_GAP between them.
        this.rightArgument.layoutXProperty().bind(this.knot.layoutXProperty().add(this.knot.radiusProperty()).add(H_GAP));
        centerLayoutVertical(this.rightArgument);

        //Put knot's drawOrder on to (above rightArgument)
        this.knot.toFront();
        //Vertically center the knot.
        this.knot.layoutYProperty().bind(this.heightProperty().divide(2));

        //Mouse listeners
        this.knot.setOnMousePressed(event -> {onKnotPressed(ConnectionCreationManager.INPUT_ID_MOUSE); event.consume();});
        this.knot.setOnMouseDragged(event -> {onKnotMoved(event); event.consume();});
        this.knot.setOnMouseReleased(event -> {onKnotReleased(ConnectionCreationManager.INPUT_ID_MOUSE); event.consume();});

        //Touch listeners
        this.knot.setOnTouchPressed(event -> {onKnotPressed(event.getTouchPoint().getId()); event.consume();});
        this.knot.setOnTouchMoved(event -> {onKnotMoved(event); event.consume();});
        this.knot.setOnTouchReleased(event -> {onKnotReleased(event.getTouchPoint().getId()); event.consume();});

        //Update the size of this Pane
        this.setPrefHeight(HEIGHT);
        this.setMaxHeight(USE_PREF_SIZE);
        this.setMinWidth(USE_PREF_SIZE);
        this.setMaxWidth(USE_PREF_SIZE);

        this.prefWidthProperty().bind(getTotalWidthProperty());

        // Since at the point of a layout update the width of the Labels is unknown, we have to ask for another layout pass.
        this.rightArgument.widthProperty().addListener(a -> Platform.runLater(block::updateLayout));

        snapToKnotIndex();
    }

    /**
     * @return DoubleBinding that represents the entire width of all the elements represented in this ArgumentSpace.
     */
    public DoubleBinding getTotalWidthProperty() {
        return rightArgument.layoutXProperty().add(rightArgument.translateXProperty()).add(rightArgument.widthProperty()).add(H_GAP);
    }

    /**
     * Binds a region's layoutY property to always be vertically centered in the ArgumentSpace.
     * @param region
     */
    private void centerLayoutVertical(Region region) {
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
     * Sets up a new drag action by storing the inputID.
     *
     * @param inputID InputID of the input that pressed the knot.
     */
    private void onKnotPressed(int inputID) {
        knot.layoutXProperty().unbind();
        if(this.inputID == ConnectionCreationManager.INPUT_ID_NONE) {
            this.inputID = inputID;
        } else {
            // Ignore, another touch point is already dragging;
        }
    }

    /**
     * To be called when the knot gets released
     * @param inputID InputID of the input that released the knot.
     */
    private void onKnotReleased(int inputID) {
        if(this.inputID == inputID) {
            this.inputID = ConnectionCreationManager.INPUT_ID_NONE;
            snapToKnotIndex();
        } else {
            // Ignore, different touch point;
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
    private void onKnotMoved(InputEvent event) {
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
            Region leftMostArg = leftArguments.isEmpty() ? rightArgument : Iterables.getLast(leftArguments);
            double rightBound = leftMostArg.getLayoutX() + leftMostArg.getWidth() + H_GAP + knot.getRadius();

            //Set the knot's new position.
            if (knot.layoutXProperty().isBound()) {
                knot.layoutXProperty().unbind();
            }
            knot.setLayoutX(Math.min(Math.max(localX, leftBound), rightBound));

            // Properly react on the change in the knot's position.
            double modifier = Math.max(-0.49, Math.min(KNOT_SNAP_MODIFIER, 0.49));
            setKnotIndex((int) Math.round(determineKnotIndex() + modifier));
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
                // Knot is positioned fully to the left of this argument.
                bowtieIndex++;
            } else {
                // Knot is positioned somewhere halfway this argument.
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
            argument.setTranslateX((H_GAP + knot.getRadius() * 2) * (1 - percentage));
            argument.setOpacity(percentage);
            argument.setVisible(percentage > 0);
        }
    }

    /**
     * Method to indicate that the content in the input and output Labels are possibly outdated.
     * TODO Update all right arguments instead of the single one
     */
    public void invalidateTypes() {
        rightArgument.setText(block.getAllOutputs().get(0).getStringType());

        for (InputArgument argument : leftArguments) {
            argument.setInputText(argument.getInputAnchor().getStringType());
        }
    }

    /**
     * @return The InputArguments this ArgumentSpace has.
     */
    public List<InputArgument> getInputArguments() {
        return leftArguments;
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
