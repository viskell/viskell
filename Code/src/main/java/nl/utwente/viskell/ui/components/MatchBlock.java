package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.OtherMatchBinder;
import nl.utwente.viskell.haskell.expr.PrimaryMatchBinder;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.CustomUIPane;

import com.google.common.collect.ImmutableList;

/**
 * 
 *
 */
public class MatchBlock extends Block {
    /**
     * 
     */
    protected List<OutputAnchor> outputs;
    
    /**
     * 
     */
    protected InputAnchor input;
    
    /** The space containing the input anchor(s). */
    @FXML protected TilePane inputSpace;

    /** The space containing the output anchor. */
    @FXML protected TilePane outputSpace;

    /** The space containing the output anchor. */
    @FXML protected Label name;

    /** The space in which to nest the FunctionBlock's inner parts. */
    @FXML protected Pane nestSpace;
    
    protected FunctionInfo info;

    protected PrimaryMatchBinder primaryBinder;
    

    public MatchBlock(FunctionInfo funInfo, CustomUIPane pane) {
        super(pane);
        this.loadFXML("MatchBlock");
        
        outputs = new ArrayList<>();
        info = funInfo;
        Type type = info.getFreshSignature();
        primaryBinder = new PrimaryMatchBinder(info.getName());
        
        int outputCount = 0;
        while (type instanceof FunType) {
            FunType ft = (FunType) type;
            
            Binder binder = new OtherMatchBinder("res"+outputCount, ft.getArgument());
            primaryBinder.addBinder(binder);
            outputs.add(new OutputAnchor(this, binder));
            type = ft.getResult();
            outputCount++;
        }
        
        input = new InputAnchor(this);
        input.setType(type);
        
        name.setText(info.getName());
        
        //TODO Fill in the type space
        
        outputSpace.getChildren().addAll(outputs);
        inputSpace.getChildren().add(input);
    }
    
    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(input);
    }
    
    @Override
    public List<OutputAnchor> getAllOutputs() {
        return outputs;
    }

    @Override
    protected void refreshAnchorTypes() {
        Type type = info.getFreshSignature();
        TypeScope scope = new TypeScope();
        
        for (OutputAnchor anchor : outputs) {
            if (type instanceof FunType) {
                FunType ftype = (FunType)type;
                anchor.setFreshRequiredType(ftype.getArgument(), scope);
                type = ftype.getResult();
            } else {
                new RuntimeException("too many arguments in this functionblock " + name.getText());
            }
        }
        
        input.setFreshRequiredType(type, scope);
    }

    @Override
    public void updateExpr() {
        localExpr = input.getLocalExpr();
    }

    @Override
    public void invalidateVisualState() {
        // TODO Propagate new visual state to anchors
    }

    public Binder getPrimaryBinder() {
        return primaryBinder;
    }

}
