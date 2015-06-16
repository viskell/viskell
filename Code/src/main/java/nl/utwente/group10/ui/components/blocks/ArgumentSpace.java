package nl.utwente.group10.ui.components.blocks;

import java.util.ArrayList;
import java.util.List;

import nl.utwente.group10.ui.components.ComponentLoader;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

//TODO make FlowPane
public class ArgumentSpace extends FlowPane implements ComponentLoader{    
    public static final int INPUT_ID_NONE = -2;
    public static final int INPUT_ID_MOUSE = -1;
    
    private int inputID;
    private FunctionBlock block;
    private IntegerProperty bowtieIndex;
    @FXML
    private Label rightArgument;
    @FXML
    private Circle knot;
    private List<Node> leftArguments;
    
    public ArgumentSpace(FunctionBlock block) {
        this.loadFXML("ArgumentSpace");
        
        this.block = block;
        this.inputID = INPUT_ID_NONE;
        this.bowtieIndex = block.bowtieIndexProperty();
        leftArguments = new ArrayList<Node>();
        bowtieIndex.addListener(event -> invalidateBowtieIndex());
        
        for (int i = 0; i < block.getAllInputs().size(); i++) {
            Label lbl = new Label(block.getInputSignature(i).toHaskellType());
            lbl.getStyleClass().add("leftArgument");
            leftArguments.add(lbl);
            this.getChildren().add(lbl);
        }

        knot.setOnMousePressed(event -> {knotPressed(INPUT_ID_MOUSE); event.consume();});
        knot.setOnMouseDragged(event -> {knotMoved(event); event.consume();});
        knot.setOnMouseReleased(event -> {knotReleased(INPUT_ID_MOUSE); event.consume();});

        knot.toFront();
        rightArgument.toFront();
        rightArgument.translateXProperty().bind(knot.translateXProperty());

        invalidateBowtieIndex();
        invalidateArgumentContent();
        
        this.setPrefHeight(50);
        this.setAlignment(Pos.CENTER_LEFT);
    }
    

    public double getKnotPos() {
        return knot.getBoundsInParent().getMaxX() - knot.getBoundsInLocal().getWidth() / 2;
    }
    
    private void knotPressed(int inputID) {
        if(this.inputID == INPUT_ID_NONE) {
            this.inputID = inputID;
        } else {
            //Ignore, another touch point is already dragging;
        }
    }
    
    private void knotReleased(int inputID) {
        if(this.inputID == inputID) {
            this.inputID = INPUT_ID_NONE;
            snapBowtie();
        } else {
            //Ignore, different touch point;
        }
    }
    
    private void knotMoved(InputEvent event) {
        if(event instanceof MouseEvent) {
            MouseEvent mEvent = (MouseEvent) event;
            knotMoved(INPUT_ID_MOUSE, mEvent.getSceneX());
        }
    }
    
    private void knotMoved(int inputID, double sceneX) {
        if(this.inputID == inputID) {
            double translateX = sceneToLocal(sceneX,0).getX();

            double leftBound = leftArguments.get(0).getLayoutX() + knot.getBoundsInLocal().getWidth() / 2;
            double rightBound = leftArguments.get(leftArguments.size()-1).getBoundsInParent().getMaxX() + knot.getBoundsInLocal().getWidth();
            double knotPosition = Math.max(leftBound,Math.min(translateX, rightBound));
            
            /* 
             * TODO (Visual) BUG:
             * When the Labels in the leftArgument change text, their size can change
             * When the size changes, the entire FlowPane to the right of the change gets repositioned.
             * Since the Knot is to the right of all the labels, the knot's layoutX can change.
             * Currently when this happens the translateX is not immediately updated.
             * This has as an effect that the knot is displaced until it gets repositioned again.
             * (Repositioning happens when dragged or released)
             */
            knot.setTranslateX(knotPosition- knot.getLayoutX());
            invalidateKnotPosition();
            
            bowtieIndex.set((int) Math.round(determineBowtieIndex()));
        }
    }
    private double determineBowtieIndex() {
        double bowtieIndex = 0;
                
        for (int i = 0; i < leftArguments.size(); i++) {
            Node argument = leftArguments.get(i);
            double min = argument.getBoundsInParent().getMinX();
            double max = argument.getBoundsInParent().getMaxX();
            
            if (getKnotPos() > max) {
                bowtieIndex++;
            } else {
                bowtieIndex += Math.max(Math.min((getKnotPos() - min) / (max - min), 1), 0);
                break;
            }
        }
        return bowtieIndex;
    }
    
    public void invalidateKnotPosition() {
        for (Node node : leftArguments) {
            if (node.getBoundsInParent().getMaxX() - node.getBoundsInLocal().getWidth() / 2 >= getKnotPos()) {
                node.setTranslateX(knot.getBoundsInLocal().getWidth() + this.getHgap());
            } else {
                node.setTranslateX(0);
            }
        }
    }
    
    public void snapBowtie() {
        int bti = (int) bowtieIndex.get();
        double snapToX;
        if (bti > 0) {
            Node leftNeighbour = leftArguments.get((int) bti - 1);
            snapToX = leftNeighbour.getBoundsInParent().getMaxX() + knot.getBoundsInLocal().getWidth() / 2 + this.getHgap();
        } else {
            Node leftNeighbour = leftArguments.get(0);
            snapToX = leftNeighbour.getLayoutX() + knot.getBoundsInLocal().getWidth() / 2;
        }
        knot.setTranslateX(snapToX - knot.getLayoutX());
        
        invalidateKnotPosition();
    }
    
    public void invalidateBowtieIndex() {
        for (int i = 0; i < leftArguments.size(); i++) {
            Node node = leftArguments.get(i);
            ObservableList<String> styleClass = node.getStyleClass();
            if (i < bowtieIndex.get()) {
                styleClass.removeAll("transparent");
            } else {
                styleClass.removeAll("transparent");
                styleClass.add("transparent");
            }
        }
    }
    public void invalidateArgumentContent() {
        invalidateOutputContent();
        invalidateInputContent();
    }
    
    public void invalidateOutputContent() {
        String text = block.getOutputType().toHaskellType();
        rightArgument.setText(text);
    }
    
    public void invalidateInputContent() {
        for (int i = 0; i < leftArguments.size(); i++) {
            setInputError(i, !block.inputTypeMatches(i));
            ((Label) leftArguments.get(i)).setText(block.getInputType(i).toHaskellType());
        }
    }
    
    public void setInputError(int index, boolean error) {
        ObservableList<String> styleClass = leftArguments.get(index).getStyleClass();
        if (error) {
            styleClass.removeAll("error");
            styleClass.add("error");
        } else {
            styleClass.removeAll("error");
        }
    }
}
