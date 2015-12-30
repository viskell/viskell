package nl.utwente.viskell.ui.components;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.Type;

public class NestedValue extends NestedBlock {

    private final ValueBlock original;
    
    private Type valueType;
    
    public NestedValue(ValueBlock original) {
        super();
        this.original = original;
        this.valueType = original.output.getType(Optional.empty()).getFresh();
        
        HBox outputSpace = new HBox(new Bond(false));
        outputSpace.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(original.getValue());
        valueLabel.setMaxWidth(150);
        valueLabel.setAlignment(Pos.CENTER);
        valueLabel.getStyleClass().add("content");
        
        VBox body = new VBox(valueLabel, outputSpace);
        body.getStyleClass().addAll("nested", "value");
        this.getChildren().add(body);
    }

    @Override
    public void refreshTypes() {
        this.valueType = this.original.output.getType(Optional.empty()).getFresh();
    }

    @Override
    public List<Type> getInputTypes() {
        return ImmutableList.of();
    }

    @Override
    public List<Type> getOutputTypes() {
        return ImmutableList.of(this.valueType);
    }

    @Override
    public Expression getExpr() {
        return new Value(this.valueType, this.original.getValue());
    }

    @Override
    public Block getOriginal() {
        return this.original;
    }

}
