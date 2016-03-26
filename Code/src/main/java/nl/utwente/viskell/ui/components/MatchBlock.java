package nl.utwente.viskell.ui.components;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import jfxtras.scene.layout.VBox;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.ConstructorBinder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ToplevelPane;

import com.google.common.collect.ImmutableList;

/** A block that matches a data constructors and yields all the elements of that constructor. */
public class MatchBlock extends Block {

    /** Information about the data constructor as if it was a function. */
    protected FunctionInfo info;

    /** The InputAnchor of this Matchblock. */
    protected InputAnchor input;

    /** A list of nodes with an OutputAnchor and Label for every constructor element. */
    protected List<Pane> outputs;
    
    /** The space containing the input anchor(s). */
    @FXML protected Pane inputSpace;

    /** The space containing the output anchor. */
    @FXML protected Pane outputSpace;

    /** The Label with the constructor name. */
    @FXML protected Label name;

    protected ConstructorBinder primaryBinder;

    protected Label inputLabel;
    
    public MatchBlock(ToplevelPane pane, FunctionInfo funInfo) {
        super(pane);
        this.loadFXML("MatchBlock");
        
        outputs = new ArrayList<>();
        info = funInfo;
        Type type = info.getFreshSignature();
        ArrayList<Binder> elemBinders = new ArrayList<>();
        
        int outputCount = 0;
        while (type instanceof FunType) {
            FunType ftype = (FunType) type;
            
            Binder binder = new Binder("res"+outputCount, ftype.getArgument());
            elemBinders.add(binder);
            OutputAnchor anchor = new OutputAnchor(this, binder);
            
            VBox box = new VBox(0);
            box.setTranslateY(9);
            box.getStyleClass().add("argumentSpace");
            Label typeLabel = new Label(ftype.getArgument().prettyPrint());
            typeLabel.getStyleClass().add("resultType");
            box.getChildren().addAll(typeLabel, anchor);
            
            outputs.add(box);
            type = ftype.getResult();
            outputCount++;
        }

        primaryBinder = new ConstructorBinder(info.getName(), elemBinders);
        
        input = new InputAnchor(this, type);
        inputLabel = new Label(info.getFreshSignature().prettyPrint());
        inputLabel.getStyleClass().add("inputType");
        
        String fname = funInfo.getDisplayName();
        name.setText(fname.charAt(0) == '(' && fname.length() > 2 ? fname.substring(1, fname.length()-1) : fname);
        
        inputSpace.getChildren().addAll(input, inputLabel);
        inputSpace.setTranslateY(-9);
        outputSpace.getChildren().addAll(outputs);
    }

    /** @return a Map of class-specific properties of this Block. */
    @Override
    protected Map<String, Object> toBundleFragment() {
        return ImmutableMap.of("funInfo", this.info.toBundleFragment());
    }

    /** return a new instance of this Block type deserializing class-specific properties used in constructor **/
    public static MatchBlock fromBundleFragment(ToplevelPane pane, Map<String,Object> bundleFragment) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> funInfoBundle = (Map<String, Object>)bundleFragment.get("funInfo");
        FunctionInfo funInfo = FunctionInfo.fromBundleFragment(funInfoBundle);
        return new MatchBlock(pane, funInfo);
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(input);
    }
    
    @Override
    public List<OutputAnchor> getAllOutputs() {
        return outputs.stream().map(box -> (OutputAnchor)(box.getChildren().get(1))).collect(Collectors.toList());
    }

    @Override
    public Optional<Block> getNewCopy() {
        return Optional.of(new MatchBlock(this.getToplevel(), this.info));
    }

    @Override
    protected void refreshAnchorTypes() {
        Type type = info.getFreshSignature();
        TypeScope scope = new TypeScope();
        
        for (Pane box : outputs) {
            if (type instanceof FunType) {
                FunType ftype = (FunType)type;
                OutputAnchor anchor = (OutputAnchor)box.getChildren().get(1);
                
                anchor.setFreshRequiredType(ftype.getArgument(), scope);
                type = ftype.getResult();
            } else {
                throw new RuntimeException("too many arguments in this matchblock " + name.getText());
            }
        }
        
        input.setFreshRequiredType(type, scope);
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        return input.getLocalExpr(outsideAnchors);
    }

    @Override
    public void invalidateVisualState() {
    	this.input.invalidateVisualState();
    	
        boolean validConnection = input.hasValidConnection();
        inputSpace.setTranslateY(validConnection ? 0 : -9);
        inputLabel.setText(validConnection ? "zyxwv" : input.getStringType()); 
        inputLabel.setVisible(!validConnection);
    	
        for (Pane box : outputs) {
            OutputAnchor anchor = (OutputAnchor)box.getChildren().get(1);
            Label label = (Label)box.getChildren().get(0);
            label.setText(anchor.getStringType());
            anchor.invalidateVisualState();
        }
    }

    public Binder getPrimaryBinder() {
        return primaryBinder;
    }

}
