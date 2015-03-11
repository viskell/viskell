/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.utwente.cs.caes.tactile.fxml;

import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import nl.utwente.cs.caes.tactile.control.Anchor;
import nl.utwente.cs.caes.tactile.control.Bond;

public class TactileBuilderFactory implements BuilderFactory {
    private final JavaFXBuilderFactory defaultBuilderFactory = new JavaFXBuilderFactory();
    
    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (type == Anchor.class) {
            return new TactilePaneAnchorBuilder();
        } else if (type == Bond.class) {
            throw new UnsupportedOperationException("Not supported yet");
        } else {
            return defaultBuilderFactory.getBuilder(type);
        }
    }
}
