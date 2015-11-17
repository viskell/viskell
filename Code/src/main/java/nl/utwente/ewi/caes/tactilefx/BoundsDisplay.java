package nl.utwente.ewi.caes.tactilefx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


class BoundsDisplay extends Pane {
    Rectangle boundsOverlay = new Rectangle();
    
    public BoundsDisplay(double width, double height) {
        setBoundsWidth(width);
        setBoundsHeight(height);
        
        boundsOverlay.widthProperty().bind(boundsWidth);
        boundsOverlay.heightProperty().bind(boundsHeight);
        
        boundsOverlay.setFill(new Color(1, 0, 0, 0));
        boundsOverlay.setStroke(new Color(1, 0, 0, 0.5));
        
        getChildren().add(boundsOverlay);
    }
    
    DoubleProperty boundsWidth = new SimpleDoubleProperty();
    
    public final void setBoundsWidth(double width) {
        boundsWidth.set(width);
    }
    
    DoubleProperty boundsHeight = new SimpleDoubleProperty();
    
    public final void setBoundsHeight(double height) {
        boundsHeight.set(height);
    }
}
