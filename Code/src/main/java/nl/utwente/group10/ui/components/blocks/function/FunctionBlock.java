package nl.utwente.group10.ui.components.blocks.function;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import nl.utwente.group10.haskell.env.FunctionInfo;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.FunVar;
import nl.utwente.group10.haskell.type.FunType;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.input.InputBlock;
import nl.utwente.group10.ui.components.blocks.output.OutputBlock;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;

/**
 * Main building block for the visual interface, this class represents a Haskell
 * function together with it's arguments and visual representation.
 */
public class FunctionBlock extends Block implements InputBlock, OutputBlock {
    /** The OutputAnchor of this FunctionBlock. */
    private OutputAnchor output;

    /** The function name. */
    private StringProperty name;
    
    /** The information about the function. */
    private FunctionInfo funInfo;

    /** The space containing the input anchor(s). */
    @FXML private Pane inputSpace;

    /** The space containing the output anchor. */
    @FXML private BorderPane outputSpace;

    /** The space containing all the arguments of the function. */
    private ArgumentSpace argumentSpace;
    
    /** The space in which to nest the FunctionBlock's inner parts. */
    @FXML private Pane nestSpace;
    
    /** The space in which the information of the function is displayed. */
    @FXML private Pane functionInfo;
    
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
        this.name = new SimpleStringProperty(funInfo.getName());
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
        output = new OutputAnchor(this);
        outputSpace.setCenter(output);
        
        // Make sure the prefWidth is correctly updated.
        this.prefWidthProperty().bind(functionInfo.widthProperty().add(argumentSpace.prefWidthProperty()));
        
        this.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        functionInfo.setMinWidth(Region.USE_PREF_SIZE);
        functionInfo.setMaxWidth(Region.USE_PREF_SIZE);
        
        // Since at this point of the width of the Labels is unknown, we have to ask for another layout pass.
        Platform.runLater(this::updateLayout);
    }
    
    /** Updates the layout, if this Pane has a parent. */
    public void updateLayout() {
        if (this.getParent() != null) {
            this.getParent().requestLayout();
        }
    }
    
    /** @return the name of this FunctionBlock. */
    public final String getName() {
        return name.get();
    }
    
    /** @return the StringProperty for the name of the function. */
    public final StringProperty nameProperty() {
        return name;
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
    @Override
    public List<InputAnchor> getActiveInputs() {
        return getAllInputs().subList(0, getKnotIndex());
    }

    /**
     * @return InputAnchor with the given index.
     */
    public InputAnchor getInput(int index) {
        return getAllInputs().get(index);
    }

    @Override
    public OutputAnchor getOutputAnchor() {
        return output;
    }

    /**
     * On top of updating the expression, this method also adds a record to the
     * CustomUIPane that maps the expr to this block. Clears the dirty flag.
     */
    @Override
    public final void updateExpr() {
        getPane().removeExprToFunction(expr);
        expr = new FunVar(this.funInfo);
        getPane().putExprToFunction(expr, this);
        
        for (InputAnchor in : getActiveInputs()) {
            expr = new Apply(expr, in.getUpdatedExpr());
        }
        super.updateExpr();
    }
    
    @Override
    public void invalidateVisualState() {
        super.invalidateVisualState();
        argumentSpace.invalidateInputContent();
        argumentSpace.invalidateOutputContent();
    }
    
    /**
     * If the latest analyze attempt was successful, remove all kept error
     * indications.
     * 
     * Since only 1 error can be detected by analyzing, only setting error state
     * to false whenever everything goes well makes it possible in some cases to
     * show multiple errors.
     */
    @Override
    public void invalidateConnectionState() {
        for (InputAnchor input : this.getAllInputs()) {
            if (!input.isPrimaryConnected()) {
                // Remove error state is not connected.
                input.setErrorState(false);
            } else if (!getPane().getErrorOccured()) {
                // Remove error state is no error occured.
                input.setErrorState(false);
            }
        }
    }

    /**
     * React to a potential state change with regards to the knot index.
     * This then activates / disables the inputs with regards to the knot index.
     */
    private void invalidateKnotIndex() {
        List<InputAnchor> inputs = getAllInputs();
        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setActiveState(i < getKnotIndex());
        }
        
        // Trigger invalidation for the now changed output type.
        this.setConnectionState(ConnectionCreationManager.nextConnectionState());
    }
    
    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    protected ImmutableMap<String, Object> toBundleFragment() {
        return ImmutableMap.of("name", getName(), "knotIndex", getKnotIndex());
    }
}
