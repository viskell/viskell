/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.ui;

import java.util.Set;
import java.util.TreeSet;
import lwbdemo.model.Term;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.cs.caes.tactile.control.TactilePane;
import nl.utwente.cs.caes.tactile.control.Anchor;

/**
 *
 * @author Richard
 */
public class Bowtie extends Group {
    private static final double BG_OFFSET = 0;
    
    final Knot knot;
    final HBox hbox;
    final TermBlade termBlade;
    final TypeBlade typeBlade;
    final Polygon background;
    
    private boolean compact = false;
    private TermDisplay anchor = null;
    private final TactilePane tracker;
    
    public Bowtie(TactilePane tracker, String name, Term... terms) {
        this.tracker = tracker;
        
        termBlade = new TermBlade(this, name);
        typeBlade = new TypeBlade(this, terms);
        knot = new Knot(this);
        
        hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(termBlade, knot, typeBlade);
                
        // Draws the bowtie
        background = new Polygon();
        background.setFill(Color.BISQUE);
        background.setStroke(Color.BROWN);
        background.setStrokeWidth(2);
        
        hbox.boundsInParentProperty().addListener((obs, oldVal, newVal) -> {
            drawBackground();
        });
        knot.boundsInParentProperty().addListener((obs, oldVal, newVal) -> {
            drawBackground();
        });
        
        getChildren().addAll(background, hbox);
        
        TactilePane.setTracker(typeBlade, tracker);
        
        // Makes sure that TouchEvents that are targeted on the Knot won't be handled
        // by Knot in case it's the only TouchPoint manipulating the Bowtie. This 
        // forces the user to use two fingers to move the Knot, one to hold the 
        // Bowtie in position, and another to move the knot.
        final Set<Integer> touchIDs = new TreeSet<>();
        addEventFilter(TouchEvent.ANY, event -> {
            int touchId = event.getTouchPoint().getId();
            
            if (event.getEventType() == TouchEvent.TOUCH_PRESSED) {
                touchIDs.add(touchId);
                if (touchIDs.size() == 1) {
                    if (event.getTarget() == knot) {
                        knot.ignoredTouchId = touchId;
                    }
                } else {
                    knot.ignoredTouchId = -1;
                    if (knot.ignoredTouchId == TactilePane.getDragContext(this).getTouchId()) {
                        TactilePane.getDragContext(this).bind(event);
                    }
                }
            } else if (event.getEventType() == TouchEvent.TOUCH_RELEASED) {
                touchIDs.remove(touchId);
                if (knot.ignoredTouchId == touchId) {
                    knot.ignoredTouchId = -1;
                }
            }
        });
    }
    
    // PROPERTIES
    
    public Term getType() {
        return typeBlade.getType();
    }
    
    void setAnchor(TermDisplay termDisplay) {
        if (termDisplay == null) {
            grow();
            
            TactilePane.setAnchor(this, null);
            TactilePane.setTracker(typeBlade, (TactilePane) getParent());
        } else {
            shrink();
            
            TactilePane.setAnchor(this, new Anchor(termDisplay, 1, 1, Pos.CENTER));
        }
        anchor = termDisplay;
    }
    
    TermDisplay getAnchor() {
        return anchor;
    }
    
    // METHODS
    
    public void exposeHole() {
        TermDisplay removeNode = typeBlade.popTerm();
        if (removeNode != null) {
            termBlade.pushTerm(removeNode);
        }
    }
    
    public void coverHole() {
        TermDisplay removeNode = termBlade.popTerm();
        if (removeNode != null) {
            typeBlade.pushTerm(removeNode);
        }
    }
    
    // HELPER METHODS
    
    private void shrink() {
        if (!compact) {
            TactilePane.setTracker(typeBlade, null);
            
            hbox.getChildren().removeAll(knot, typeBlade);
            termBlade.setPadding(Insets.EMPTY);
            
            compact = true;
            
            background.setFill(Color.LIGHTGREEN);
        }
    }
    
    private void grow() {
        if (compact) {
            hbox.getChildren().addAll(knot, typeBlade);
            termBlade.setPadding(new Insets(10));
            
            TactilePane.setTracker(typeBlade, tracker);
            
            compact = false;
            
            background.setFill(Color.BISQUE);
            drawBackground();
        }
    }
    
    private void drawBackground() {
        Bounds hboxBounds = hbox.getBoundsInParent();
        background.getPoints().clear();
        if (compact) {
            background.getPoints().addAll(new Double[]{
                hboxBounds.getMinX(), hboxBounds.getMinY(),
                hboxBounds.getMaxX(), hboxBounds.getMinY(),
                hboxBounds.getMaxX(), hboxBounds.getMaxY(),
                hboxBounds.getMinX(), hboxBounds.getMaxY()
            });
        } else {
            Bounds knotBounds = knot.getBoundsInParent();

            background.getPoints().addAll(new Double[]{
                // Top left corner
                hboxBounds.getMinX(), hboxBounds.getMinY(),
                knotBounds.getMinX(), hboxBounds.getMinY(),
                knotBounds.getMinX() + knotBounds.getWidth() / 2, hboxBounds.getMinY() + hboxBounds.getHeight() / 2,
                knotBounds.getMaxX(), hboxBounds.getMinY(),
                // Top right corner
                hboxBounds.getMaxX(), hboxBounds.getMinY(),
                // Bottom right corner
                hboxBounds.getMaxX(), hboxBounds.getMaxY(),
                knotBounds.getMaxX(), hboxBounds.getMaxY(),
                knotBounds.getMinX() + knotBounds.getWidth() / 2, hboxBounds.getMinY() + hboxBounds.getHeight() / 2,
                knotBounds.getMinX(), hboxBounds.getMaxY(),
                // Bottom left corner
                hboxBounds.getMinX(), hboxBounds.getMaxY()
            });
        }
    }
}
