/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.utwente.cs.caes.tactile.skin;

import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import nl.utwente.cs.caes.tactile.control.TactilePane;

public class TactilePaneSkin extends SkinBase<TactilePane> {
    TactilePane pane;
    
    public TactilePaneSkin(final TactilePane tactilePane) {
        super(tactilePane);
        
        pane = tactilePane;
        
        consumeMouseEvents(false);
    }
    
    /**
     * Called during the layout pass of the scenegraph. 
     */
    @Override
    protected void layoutChildren(final double contentX, final double contentY,
            final double contentWidth, final double contentHeight) {
        
        // Like a Pane, it will only set the size of managed, resizable content 
        // to their preferred sizes and does not do any node positioning.
        pane.getChildren().stream()
            .filter(Node::isResizable)
            .filter(Node::isManaged)
            .forEach(Node::autosize);
    }
}
