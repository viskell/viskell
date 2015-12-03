package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.ConstructorBinder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.CustomUIPane;

import com.google.common.collect.ImmutableList;

/** A block that matches a data constructors and yields all the elements of that constructor. */
public class MatchBlock extends Block {

    /** Information about the data constructor as if it was a function. */
    protected FunctionInfo info;

    /** The InputAnchor of this Matchblock. */
    protected InputAnchor input;

    /** A list of OutputAnchors for every constructor element. */
    protected List<OutputAnchor> outputs;
    
    /** The space containing the input anchor(s). */
    @FXML protected Pane inputSpace;

    /** The space containing the output anchor. */
    @FXML protected Pane outputSpace;

    /** The Label with the constructor name. */
    @FXML protected Label name;

    protected ConstructorBinder primaryBinder;
    
    public MatchBlock(FunctionInfo funInfo, CustomUIPane pane) {
        super(pane);
        this.loadFXML("MatchBlock");
        
        outputs = new ArrayList<>();
        info = funInfo;
        Type type = info.getFreshSignature();
        ArrayList<Binder> elemBinders = new ArrayList<>();
        
        int outputCount = 0;
        while (type instanceof FunType) {
            FunType ft = (FunType) type;
            
            Binder binder = new Binder("res"+outputCount, ft.getArgument());
            elemBinders.add(binder);
            outputs.add(new OutputAnchor(this, binder));
            type = ft.getResult();
            outputCount++;
        }

        primaryBinder = new ConstructorBinder(info.getName(), elemBinders);
        
        input = new InputAnchor(this, type);
        
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
                throw new RuntimeException("too many arguments in this matchblock " + name.getText());
            }
        }
        
        input.setFreshRequiredType(type, scope);
    }

    @Override
    public Pair<Expression, Set<Block>> getLocalExpr() {
        return input.getLocalExpr();
    }

    @Override
    public void invalidateVisualState() {
        // TODO Propagate new visual state to anchors
    }

    public Binder getPrimaryBinder() {
        return primaryBinder;
    }

}
