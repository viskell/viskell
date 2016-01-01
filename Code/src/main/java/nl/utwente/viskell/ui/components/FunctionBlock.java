package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.ToplevelPane;

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
    private FunctionReference funRef;

    /** The space containing the input anchor(s). */
    @FXML private Pane inputSpace;

    /** The space containing the output anchor. */
    @FXML private Pane outputSpace;

    /** The space containing the body of the function. */
    @FXML private Pane bodySpace;
    
    /** The space containing all the arguments of the function. */
    private ArgumentSpace argumentSpace;
    
    /** The space in which to nest the FunctionBlock's inner parts. */
    @FXML private Pane nestSpace;
    
    /**
     * Method that creates a newInstance of this class along with it's visual
     * representation
     *
     * @param funInfo
     *            The information about the function.
     * @param pane
     *            The parent pane in which this FunctionBlock exists.
     */
    public FunctionBlock(FunctionReference funRef, ToplevelPane pane) {
        super(pane);
        this.funRef = funRef;
        funRef.initializeBlock(this);

        this.loadFXML("FunctionBlock");
        this.bodySpace.getChildren().add(0, funRef.asRegion());

        // Collect argument types
        ArrayList<String> args = new ArrayList<>();
        Type t = funRef.refreshedType(funRef.requiredArguments(), new TypeScope());
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
        
        // Make sure the prefWidth is correctly updated.
        this.prefWidthProperty().bind(funRef.asRegion().widthProperty().add(argumentSpace.prefWidthProperty()));
        
    }

    public FunctionReference getFunReference() {
        return this.funRef;
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
        List<InputAnchor> res = new ArrayList<>(); 
        argumentSpace.getInputArguments().stream().map(a -> a.getInputAnchor()).collect(Collectors.toCollection(() -> res));
        this.funRef.getInputAnchor().ifPresent(fia -> res.add(0, fia));
        return res;
    }

    /**
     * @return Only the active (as specified by the knot index) inputs.
     */
    public List<InputAnchor> getActiveInputs() {
        return argumentSpace.getInputArguments().stream().map(a -> a.getInputAnchor()).collect(Collectors.toList()).subList(0, getKnotIndex());
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of(output);
    }

    @Override
    public Optional<Block> getNewCopy() {
        if (this.argumentSpace.getKnotIndex() == this.getAllInputs().size()) {
            return Optional.of(new FunctionBlock(this.funRef.getNewCopy(), this.getToplevel()));
        }
    
        return Optional.empty();
    }
    
    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        Expression expr = this.funRef.getLocalExpr(outsideAnchors);
        
        for (InputAnchor in : this.getActiveInputs()) {
            expr = new Apply(expr, in.getLocalExpr(outsideAnchors));
        }
        
       // TODO: deal with this in getLocalExpr for LocalDefUse
       // outsideAnchors.addAll(funInfo.getRequiredBlocks().stream().flatMap(block -> block.getAllOutputs().stream()).collect(Collectors.toList()));
        
        return expr;
    }
    
    @Override
    public void refreshAnchorTypes() {
        TypeScope scope = new TypeScope();
        Type type = this.funRef.refreshedType(argumentSpace.getInputArguments().size(), scope);
        for (InputAnchor arg : this.getActiveInputs()) {
            if (type instanceof FunType) {
                FunType ftype = (FunType)type;
                arg.setFreshRequiredType(ftype.getArgument(), scope);
                type = ftype.getResult();
            } else {
                new RuntimeException("too many arguments in this functionblock " + funRef.getName());
            }
        }
        this.output.setFreshRequiredType(type, scope);
    }

    @Override
    public void invalidateVisualState() {
        this.funRef.invalidateVisualState();
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
        return this.funRef.getName();
    }

    @Override
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of("name", this.funRef.getName(), "knotIndex", getKnotIndex());
    }
}
