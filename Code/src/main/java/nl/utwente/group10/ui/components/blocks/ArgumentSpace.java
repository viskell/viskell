package nl.utwente.group10.ui.components.blocks;

import java.util.ArrayList;
import java.util.List;

import nl.utwente.group10.ui.components.ComponentLoader;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class ArgumentSpace extends Pane implements ComponentLoader{    
    public static final int INPUT_ID_NONE = -2;
    public static final int INPUT_ID_MOUSE = -1;
    
    public static final int H_GAP = 10;
    
    /**
     * The index of the bowtie, all inputs with index higher or equal to the
     * bowtie are be inactive.
     */
    private IntegerProperty bowtieIndex;
    
    private int inputID;
    private FunctionBlock block;
    @FXML
    private Label rightArgument;
    @FXML
    private Circle knot;
    private List<Region> leftArguments;
    
    public ArgumentSpace(FunctionBlock block) {
        this.loadFXML("ArgumentSpace");
        
        this.block = block;
        this.inputID = INPUT_ID_NONE;
        leftArguments = new ArrayList<Region>();
        bowtieIndex = new SimpleIntegerProperty(0);
        bowtieIndex.addListener(event -> invalidateBowtieIndex());        
        
        for (int i = 0; i < block.getAllInputs().size(); i++) {
            Label lbl = new Label(block.getInputSignature(i).toHaskellType());
            lbl.getStyleClass().add("leftArgument");
            leftArguments.add(lbl);
            centerLayoutVertical(lbl);
            
            if (i > 0) {
                Region prev = leftArguments.get(i-1);
                lbl.layoutXProperty().bind(prev.layoutXProperty().add(prev.translateXProperty()).add(prev.widthProperty()).add(H_GAP));
            } else {
                //lbl.layoutXProperty().bind(knot.radiusProperty().multiply(2).add(H_GAP));
            }
            
            this.getChildren().add(lbl);
        }

        knot.setOnMousePressed(event -> {knotPressed(INPUT_ID_MOUSE); event.consume();});
        knot.setOnMouseDragged(event -> {knotMoved(event); event.consume();});
        knot.setOnMouseReleased(event -> {knotReleased(INPUT_ID_MOUSE); event.consume();});

        rightArgument.toFront();
        rightArgument.layoutXProperty().bind(knot.layoutXProperty().add(knot.radiusProperty()).add(H_GAP));
        centerLayoutVertical(rightArgument);
        
        knot.toFront();
        knot.layoutYProperty().bind(this.heightProperty().divide(2));

        invalidateBowtieIndex();
        //invalidateArgumentContent();
        
        this.setPrefHeight(50);
        this.setMaxHeight(USE_PREF_SIZE);
        this.prefWidthProperty().bind(getTotalWidthProperty());
        this.minWidthProperty().bind(getTotalWidthProperty());
        //this.prefWidthProperty().addListener(p -> System.out.println(this.getPrefWidth()));
        //TODO: PrefWidth is properly updating, yet the total space allocated to the ArgumentSpace is not.
        
        snapBowtie();
    }
    
    public ObservableValue<? extends Number> getTotalWidthProperty() {       
        return rightArgument.layoutXProperty().add(rightArgument.translateXProperty()).add(rightArgument.widthProperty()).add(H_GAP);
    }
    
    public void centerLayoutVertical(Region region) {
        region.layoutYProperty().bind(this.heightProperty().divide(2).subtract(region.heightProperty().divide(2)));
    }
    

    public double getKnotPos() {
        return knot.getLayoutX() - knot.getRadius();
    }
    
    private void knotPressed(int inputID) {
        knot.layoutXProperty().unbind();
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
            double leftBound = 0 + knot.getRadius();
            Region leftMostArg = leftArguments.get(leftArguments.size() - 1);
            double rightBound = leftMostArg.getLayoutX() + leftMostArg.getWidth() + H_GAP + knot.getRadius();
            double knotPosition = Math.min(Math.max(translateX, leftBound), rightBound);
            
            knot.setLayoutX(knotPosition);
            
            setBowtieIndex((int) Math.round(determineBowtieIndex()));
            invalidateKnotPosition();
        }
    }
    private double determineBowtieIndex() {
        double bowtieIndex = 0;
                
        for (int i = 0; i < leftArguments.size(); i++) {
            Node argument = leftArguments.get(i);
            double min = argument.getLayoutX();
            double max = min + argument.getBoundsInLocal().getWidth();
            
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
        double bti = determineBowtieIndex();
        for (int i = 0; i < leftArguments.size(); i++) {
            Node node = leftArguments.get(i);            
            double percentage = Math.min(Math.max(bti - i,  0), 1);            
            percentage = Math.pow(percentage, 0.4); //Better looking curve
            
            if (node.translateXProperty().isBound()) {
                node.translateXProperty().unbind();
            }
            node.setTranslateX((H_GAP +knot.getRadius() * 2) * (1 - percentage));
            
            node.setVisible(percentage > 0);            
            node.setStyle("-fx-text-fill: rgba(0,0,0," + percentage + ");" 
                        + "-fx-background-color: rgba(255,255,255," + percentage + ");"
                        + "-fx-border-color: rgba(0,0,0," + percentage + ");");
        }
    }
    
    public void snapBowtie() {
        int bti = getBowtieIndex();
        for (int i = 0; i < leftArguments.size(); i++) {
            Region arg = leftArguments.get(i);
            if(bti == i) {
                arg.translateXProperty().bind(knot.radiusProperty().multiply(2).add(H_GAP));
            } else {
                arg.translateXProperty().unbind();
                arg.setTranslateX(0);
                if (bti-1 == i) {
                    knot.layoutXProperty().bind(arg.layoutXProperty().add(arg.widthProperty()).add(knot.radiusProperty()).add(H_GAP));
                }
            }
        }
        
        //bti-1 == -1, would never occur in the for loop's index
        if (bti == 0) {
            knot.setLayoutX(knot.getRadius());
        }
        invalidateKnotPosition();
    }
    
    public void invalidateBowtieIndex() {
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
    
    public Region getInputArgument(int index) {
        return leftArguments.get(index);
    }
    
    public Region getOutputArgument() {
        return rightArgument;
    }

    public void setBowtieIndex(int index) {
        bowtieIndex.set(index);        
    }
    
    public int getBowtieIndex() {
        return bowtieIndex.get();        
    }
    
    public IntegerProperty bowtieIndexProperty() {
        return bowtieIndex;
    }
}
