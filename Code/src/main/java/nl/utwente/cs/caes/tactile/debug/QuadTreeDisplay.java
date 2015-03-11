/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.utwente.cs.caes.tactile.debug;

import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import nl.utwente.cs.caes.tactile.control.QuadTree;

public class QuadTreeDisplay extends Pane{
    private Rectangle r;
    private Label l;
    
    public QuadTreeDisplay(QuadTree quadTree, String label) {
        Bounds b = quadTree.getBounds();
        r = new Rectangle(b.getWidth(), b.getHeight());
        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.GREEN);
        r.setStrokeWidth(1);
        r.setStrokeType(StrokeType.CENTERED);
        
        l = new Label(label + ": " + quadTree.proximityBoundsByObject.keySet().size());
        l.relocate(b.getWidth() / 2 - l.getWidth() / 2, b.getHeight() / 2 - l.getHeight() / 2);
        
        getChildren().addAll(l, r);
    }
}
