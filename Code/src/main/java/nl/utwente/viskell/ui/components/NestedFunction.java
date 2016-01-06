package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.FunVar;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;

public class NestedFunction extends NestedBlock {

    private final FunctionInfo funInfo; // TODO: maybe use a FunctionReference here instead??
    
    private final Block original;
    
    private List<Type> inputTypes;
    
    private Type outputType;
    
    public NestedFunction(FunctionInfo funInfo, Block original) {
        super();
        this.funInfo = funInfo;
        this.original = original;
        this.refreshTypes();
        
        HBox inputSpace = new HBox(60);
        inputSpace.setAlignment(Pos.CENTER);
        for (int i = 0; i < this.inputTypes.size(); i++) {
            inputSpace.getChildren().add(new Bond(true));
        }
        
        HBox outputSpace = new HBox(new Bond(false));
        outputSpace.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(funInfo.getDisplayName());
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.getStyleClass().add("content");
        
        VBox body = new VBox(inputSpace, nameLabel, outputSpace);
        body.getStyleClass().addAll("nested", "function");
        body.setAlignment(Pos.CENTER);
        this.getChildren().add(body);
    }

    @Override
    public void refreshTypes() {
        Type type = this.funInfo.getFreshSignature();
        this.inputTypes = new ArrayList<>();
        while (type instanceof FunType) {
            FunType ftype = (FunType)type;
            this.inputTypes.add(ftype.getArgument());
            type = ftype.getResult();
        }
        this.outputType = type;
    }

    @Override
    public List<Type> getInputTypes() {
        return this.inputTypes;
    }

    @Override
    public List<Type> getOutputTypes() {
        return ImmutableList.of(this.outputType);
    }

    @Override
    public Expression getExpr() {
        return new FunVar(this.funInfo);
    }

    @Override
    public Block getOriginal() {
        return this.original;
    }

}
