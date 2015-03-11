package tactiledemo;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import nl.utwente.cs.caes.tactile.control.TactilePane;
import nl.utwente.cs.caes.tactile.control.Anchor;
import nl.utwente.cs.caes.tactile.event.TactilePaneEvent;

/**
 * A Node that reacts on ColorItems. ColorItems can be dropped on
 * a ColorSlot, and will then be anchored to it.
 */
public class ColorSlot extends Rectangle {
    ColorSlotPane parent;
    Map<ColorItem, ChangeListener<Boolean>> dropListenerByColorItem = new HashMap<>();
    
    public ColorSlot(ColorSlotPane parent) {
        super(50, 50);
        
        this.parent = parent;
        setFill(Color.DARKGREY);
        setStroke(Color.DARKGREY);
        setStrokeWidth(4);
        
        TactilePane.setOnInProximity(this, event -> onInProximity(event));
        TactilePane.setOnProximityLeft(this, event -> onProximityLeft(event));
        TactilePane.setOnAreaEntered(this, event -> onAreaEntered(event));
        TactilePane.setOnAreaLeft(this, event -> onAreaLeft(event));
    }
    
    /**
     * The ColorItem that is anchored to this slot
     */
    private ObjectProperty<ColorItem> colorItem;
    
    public ColorItem getColorItem() {
        return colorItemProperty().get();
    }
    
    public void setColorItem(ColorItem value) {
        colorItemProperty().set(value);
    }
    
    public ObjectProperty<ColorItem> colorItemProperty() {
        if (colorItem == null) {
            colorItem = new SimpleObjectProperty<>();
        }
        return colorItem;
    }
    
    private void onInProximity(TactilePaneEvent event) {
        Node other = event.getOther();
        
        if (other instanceof ColorItem) {
            ColorItem colorItem = (ColorItem) other;
            
            // If the ColorSlotPane this slot belongs to is grey, then
            // its border will be set to the approaching ColorItem's color
            // If the ColorSlotPane has a different color from the approaching
            // ColorItem, then it should flee away
            if (parent.getBackgroundColor() == Color.GREY) {
                parent.setBorderColor(colorItem.getColor());
            } else if (!parent.getBorderColor().equals(colorItem.getColor())) {
                TactilePane.moveAwayFrom(this, other, 500);
            }
        }
    }
    
    private void onProximityLeft(TactilePaneEvent event) {
        Node other = event.getOther();
        
        if (other instanceof ColorItem) {
            ColorItem colorItem = (ColorItem) other;
            
            // Set the ColorSlotPane's border back to grey if it's not hosting
            // another ColorItem
            if (parent.getBackgroundColor() == Color.GREY) {
                parent.setBorderColor(Color.GREY);
            }
        }
    }
    
    private void onAreaEntered(TactilePaneEvent event) {
        Node other = event.getOther();
        
        if (other instanceof ColorItem) {
            ColorItem colorItem = (ColorItem) other;
            
            // When a ColorItem enters the area, call onDropped when its DragPane
            // is not in use anymore. That way only ColorItems that are actively
            // dragged and dropped in a slot will be accepted, rather than any
            // ColorItem that enters the area
            ChangeListener<Boolean> listener = (observable, oldVal, newVal) -> {
                if (!newVal) {
                    onDropped(colorItem);
                }
            };
            TactilePane.inUseProperty(colorItem).addListener(listener);
            dropListenerByColorItem.put(colorItem, listener);
        }
    }
    
    private void onAreaLeft(TactilePaneEvent event) {
        Node other = event.getOther();
        
        if (other instanceof ColorItem) {
            ColorItem colorItem = (ColorItem) other;
            
            //Stop listening for drag and drop operation
            ChangeListener<Boolean> listener = dropListenerByColorItem.remove(colorItem);
            TactilePane.inUseProperty(colorItem).removeListener(listener);
            
            // If the Node that has left the area of the ColorSlot is the
            // ColorItem that is anchored to it, then ColorItem will be set to null
            if (colorItem == getColorItem()) {
                setColorItem(null);
            }
        }
    }
    
    private void onDropped(ColorItem colorItem) {
        // If the ColorSlot has room for a ColorItem, then that ColorItem
        // will be anchored to that ColorSlot
        if (getColorItem() == null) {
            setColorItem((ColorItem) colorItem);
            TactilePane.setAnchor(colorItem, new Anchor(this, Pos.CENTER));
        }
    }
}
