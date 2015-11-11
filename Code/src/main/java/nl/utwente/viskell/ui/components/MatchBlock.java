package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
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
    
    protected StringProperty name;
    
    /** The space containing the input anchor(s). */
    @FXML private TilePane inputSpace;

    /** The space containing the output anchor. */
    @FXML private TilePane outputSpace;

    /** The space in which to nest the FunctionBlock's inner parts. */
    @FXML private Pane nestSpace;
    

    public MatchBlock(FunctionInfo funInfo, CustomUIPane pane) {
        super(pane);
        this.loadFXML("MatchBlock");
        
        outputs = new ArrayList<>();
        
        Type type = funInfo.getFreshSignature();
        int outputCount = 0;
        while (type instanceof FunType) {
            FunType ft = (FunType) type;
            outputs.add(new OutputAnchor(this, new Binder("res"+outputCount, ft.getArgument())));
            type = ft.getResult();
            outputCount++;
        }
        
        input = new InputAnchor(this);
        input.setType(type);
        
        name = new SimpleStringProperty("DERP "+funInfo.getName());

        /*ArgumentSpace typeSpace = new ArgumentSpace(this, outputCount);
        typeSpace.setKnotIndex(getAllInputs().size());
        
        nestSpace.getChildren().add(typeSpace);
        typeSpace.knotIndexProperty().addListener(e -> invalidateKnotIndex());*/
        
        outputSpace.getChildren().addAll(outputs);
        inputSpace.getChildren().add(input);
        
        // Since at this point of the width of the Labels is unknown, we have to ask for another layout pass.
        Platform.runLater(this::updateLayout);
    }
    
    /** Updates the layout, if this Pane has a parent. */
    public void updateLayout() {
        if (this.getParent() != null) {
            this.getParent().requestLayout();
        }
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
        // TODO Auto-generated method stub

    }

    @Override
    public void updateExpr() {
        // TODO Auto-generated method stub

    }

    @Override
    public void invalidateVisualState() {
        // TODO Auto-generated method stub

    }

}
