
package tactiledemo;


import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class ColorItem extends Rectangle {
    
    public ColorItem() {
        super(50, 50);
    }
    
    public void setColor(Paint color) {
        colorProperty().set(color);
    }
    
    public Paint getColor() {
        return colorProperty().get();
    }
    
    public ObjectProperty<Paint> colorProperty() {
        return super.fillProperty();
    }
}
