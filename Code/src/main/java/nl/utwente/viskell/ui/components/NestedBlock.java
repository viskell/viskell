package nl.utwente.viskell.ui.components;

import java.util.List;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;

public abstract class NestedBlock extends StackPane {

    private Block wrapper;
    
    protected void setWrapper(Block wrapper) {
        this.wrapper = wrapper;
    }

    public Block getWrapper() {
        return this.wrapper;
    }
    
    public abstract void refreshTypes();
    
    public abstract List<Type> getInputTypes();
    
    public abstract List<Type> getOutputTypes();
    
    public abstract Expression getExpr();
    
    public abstract Block getOriginal();
    
    protected static class Bond extends StackPane {

        public Bond(boolean isInput) {
            super();
            this.setMinSize(0, 0);
            this.setMaxSize(0, 0);
            Ellipse bond = new Ellipse(7, 3);
            bond.setFill(Color.BLACK);
            bond.setTranslateY(isInput ? -3 : 3);
            this.getChildren().add(bond);
        }
    }

}
