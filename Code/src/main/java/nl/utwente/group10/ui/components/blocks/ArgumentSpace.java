package nl.utwente.group10.ui.components.blocks;

import java.util.ArrayList;
import java.util.List;

import nl.utwente.group10.ui.components.ComponentLoader;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class ArgumentSpace extends Pane implements ComponentLoader{    
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
            lbl.widthProperty().addListener(x -> invalidateArgumentSpacing());
            this.getChildren().add(lbl);
        }

        knot.setOnMousePressed(event -> {knotPressed(INPUT_ID_MOUSE); event.consume();});
        knot.setOnMouseDragged(event -> {knotMoved(event); event.consume();});
        knot.setOnMouseReleased(event -> {knotReleased(INPUT_ID_MOUSE); event.consume();});

        invalidateBowtieIndex();
        invalidateArgumentContent();
        
        
        this.setPrefHeight(50);
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
            invalidateBowtieIndex();
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
            bowtieIndex.set(determineBowtieIndex(translateX));
            

            double knotWidth = knot.getBoundsInLocal().getWidth();
            double leftBound = leftArguments.get(0).prefWidth(-1) + knotWidth/2;
            double rightBound = getLeftWidth() + knotWidth/2;
            
            double knotPosition = Math.max(leftBound,Math.min(translateX, rightBound));
            knot.setTranslateX(knotPosition);
        }
    }
    
    private int determineBowtieIndex(double xOffset) {
        int bowtieIndex = 1;
        double leftWidth = 0;
        for (int i = 0; i < leftArguments.size(); i++) {  
            leftWidth += leftArguments.get(i).prefWidth(-1);
            if(leftWidth < xOffset) {
                bowtieIndex = i+1;
            }
        }
        return bowtieIndex;
    }
    
    public double getLeftWidth() {
        double leftWidth = 0;
        for (int i = 0; i < leftArguments.size(); i++) {  
            leftWidth += leftArguments.get(i).prefWidth(-1);
        }
        return leftWidth;
    }
    
    public void invalidateBowtieIndex() {
        for (int i = 0; i < leftArguments.size(); i++) {
            Node node = leftArguments.get(i);
            if (i < bowtieIndex.get()) {
                node.setVisible(true);
            } else {
                node.setVisible(false);
            }
        }
        invalidateArgumentSpacing();
    }
    
    public void invalidateArgumentSpacing() {
        double leftWidth = 0;
        double knotWidth = knot.getBoundsInLocal().getWidth();
        double midY = this.getPrefHeight()/2;
        for (int i = 0; i < leftArguments.size(); i++) {
            Node node = leftArguments.get(i);
            if (i < bowtieIndex.get()) {
                double nodeWidth = node.prefWidth(-1);
                node.setTranslateX(leftWidth);   
                node.setTranslateY(midY - node.prefHeight(-1)/2);
                leftWidth += nodeWidth;
            }
        }
        knot.setTranslateX(leftWidth + knotWidth/2);
        knot.setTranslateY(midY);
        
        leftWidth += knotWidth;
        rightArgument.setTranslateX(leftWidth);
        rightArgument.setTranslateY(midY - rightArgument.prefHeight(-1)/2);
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
        System.out.println(leftArguments.get(index));
    }
}
