package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Apply;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.FunVar;
import nl.utwente.viskell.haskell.type.FunType;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.CustomUIPane;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Main building block for the visual interface, this class represents a Haskell
 * function together with it's arguments and visual representation.
 */
public class FunctionBlock extends Block {
    /** The OutputAnchor of this FunctionBlock. */
    private OutputAnchor output;
    
    /** The information about the function. */
    private FunctionInfo funInfo;

    /** The space containing the input anchor(s). */
    @FXML private Pane inputSpace;

    /** The space containing the output anchor. */
    @FXML private Pane outputSpace;

    /** The space containing all the arguments of the function. */
    private ArgumentSpace argumentSpace;
    
    /** The space in which to nest the FunctionBlock's inner parts. */
    @FXML private Pane nestSpace;
    
    /** The Label in which the information of the function is displayed. */
    @FXML private Label functionInfo;
    
    /**
     * Method that creates a newInstance of this class along with it's visual
     * representation
     *
     * @param funInfo
     *            The information about the function.
     * @param pane
     *            The parent pane in which this FunctionBlock exists.
     */
    public FunctionBlock(FunctionInfo funInfo, CustomUIPane pane) {
        super(pane);
        this.funInfo = funInfo;

        this.loadFXML("FunctionBlock");

        // Collect argument types
        ArrayList<String> args = new ArrayList<>();
        Type t = funInfo.getFreshSignature();
        int inputCount = 0;
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            args.add(ft.getArgument().prettyPrint());
            t = ft.getResult();
            inputCount++;
        }
        
        argumentSpace = new ArgumentSpace(this, inputCount);
        argumentSpace.setKnotIndex(getAllInputs().size());
        
        nestSpace.getChildren().add(argumentSpace);
        argumentSpace.knotIndexProperty().addListener(e -> invalidateKnotIndex());
        
        // Create an anchor for the result
        output = new OutputAnchor(this, new Binder("res"));
        outputSpace.getChildren().add(output);
        
        functionInfo.setText(funInfo.getName());

        // Make sure the prefWidth is correctly updated.
        this.prefWidthProperty().bind(functionInfo.widthProperty().add(argumentSpace.prefWidthProperty()));
        
        functionInfo.setMinWidth(Region.USE_PREF_SIZE);
        functionInfo.setMaxWidth(Region.USE_PREF_SIZE);
        
    }
    
    /** Updates the layout, if this Pane has a parent. */
    public void updateLayout() {
        if (this.getParent() != null) {
            this.getParent().requestLayout();
        }
    }

    /** @return the knot index of this FunctionBlock. */
    public final Integer getKnotIndex() {
        return argumentSpace.knotIndexProperty().get();
    }

    @Override
    public final List<InputAnchor> getAllInputs() {
        return argumentSpace.getInputArguments().stream().map(a -> a.getInputAnchor()).collect(Collectors.toList());
    }

    /**
     * @return Only the active (as specified by the knot index) inputs.
     */
    public List<InputAnchor> getActiveInputs() {
        return getAllInputs().subList(0, getKnotIndex());
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of(output);
    }

    @Override
    public Expression getLocalExpr() {
        Expression expr = new FunVar(this.funInfo);
        for (InputAnchor in : this.getActiveInputs()) {
            expr = new Apply(expr, in.getLocalExpr());
        }
        
        return expr;
    }
    
    @Override
    public void refreshAnchorTypes() {
        Type type = this.funInfo.getFreshSignature();
        TypeScope scope = new TypeScope();
        for (InputAnchor arg : this.getActiveInputs()) {
            if (type instanceof FunType) {
                FunType ftype = (FunType)type;
                arg.setFreshRequiredType(ftype.getArgument(), scope);
                type = ftype.getResult();
            } else {
                new RuntimeException("too many arguments in this functionblock " + funInfo.getName());
            }
        }
        this.output.setFreshRequiredType(type, scope);
    }

    @Override
    public void invalidateVisualState() {
        this.argumentSpace.invalidateTypes();
    }
    
    /**
     * React to a potential state change with regards to the knot index.
     * This then activates / disables the inputs with regards to the knot index.
     */
    private void invalidateKnotIndex() {
        List<InputAnchor> inputs = getAllInputs();
        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).toggleActiveState(i < getKnotIndex());
        }
        
        // Trigger invalidation for the now changed output type.
        this.initiateConnectionChanges();
    }
    
    @Override
    public String toString() {
        return funInfo.getName();
    }

    @Override
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of("name", funInfo.getName(), "knotIndex", getKnotIndex());
    }
}
