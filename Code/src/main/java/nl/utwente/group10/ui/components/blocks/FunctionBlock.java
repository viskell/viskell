package nl.utwente.group10.ui.components.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.BackendUtils;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.exceptions.FunctionDefinitionException;
import nl.utwente.group10.ui.exceptions.TypeUnavailableException;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;

/**
 * Main building block for the visual interface, this class represents a Haskell
 * function together with it's arguments and visual representation.
 */
public class FunctionBlock extends Block implements InputBlock, OutputBlock {
    private OutputAnchor output;

    /** The function name. **/
    private StringProperty name;

    /** The type of this Function. **/
    private StringProperty type;

    /** The space containing the input anchor(s). */
    @FXML
    private Pane inputSpace;

    /** The space containing the output anchor. */
    @FXML
    private Pane outputSpace;

    /** The space containing all the argument fields of the function. */
    private ArgumentSpace argumentSpace;

    @FXML private Pane inputTypesSpace;

    @FXML private Pane outputTypesSpace;

    /**
     * Method that creates a newInstance of this class along with it's visual
     * representation
     *
     * @param name
     *            The name of the function.
     * @param type
     *            The function's type (usually a FuncT).
     * @param pane
     *            The parent pane in which this FunctionBlock exists.
     */
    public FunctionBlock(String name, Type type, CustomUIPane pane) {
        super(pane);

        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type.toHaskellType());

        this.loadFXML("FunctionBlock");
        //((Label) this.lookup("functionTitle")).setText(name);

        // Collect argument types
        ArrayList<String> args = new ArrayList<>();
        Type t = type;
        while (t instanceof FuncT) {
            FuncT ft = (FuncT) t;
            args.add(ft.getArgs()[0].toHaskellType());
            t = ft.getArgs()[1];
        }
        
        List<Type> inputSignatures = new ArrayList<>();
        Type functionSignature = getFunctionSignature();
        for (int i = 0; i < args.size(); i++) {
            if (functionSignature instanceof FuncT) {
                Type inputSignature = BackendUtils.dive((FuncT) functionSignature, i + 1);
                inputSignatures.add(inputSignature);
            } else {
                throw new FunctionDefinitionException();
            }
        }
        
        argumentSpace = new ArgumentSpace(this, inputSignatures);
        argumentSpace.setKnotIndex(getAllInputs().size());
        argumentSpace.snapToKnotIndex();
        ((Pane) this.lookup("#nestSpace")).getChildren().add(argumentSpace);
        argumentSpace.knotIndexProperty().addListener(e -> invalidateBowtieIndex());
        
        // Create an anchor for the result
        output = new OutputAnchor(this, getOutputSignature());
        output.layoutXProperty().bind(outputSpace.widthProperty().divide(2));
        outputSpace.getChildren().add(output);
    }

    /**
     * @param name
     *            The name of this FunctionBlock
     */
    public final void setName(String name) {
        this.name.set(name);
    }

    /**
     * @param type
     *            The new Haskell type for this FunctionBlock.
     */
    public final void setType(String type) {
        this.type.set(type);
    }

    /**
     * @param index
     *            The new bowtie index for this FunctionBlock.
     */
    public final void setBowtieIndex(int index) {
        if (index >= -1 && index <= getAllInputs().size()) {
            argumentSpace.setKnotIndex(index);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public OutputAnchor getOutputAnchor() {
        return output;
    }

    /** Returns the array of input anchors for this function block. */
    @Override
    public final List<InputAnchor> getAllInputs() {
        return argumentSpace.getInputArguments().stream().map(a -> a.getInputAnchor()).collect(Collectors.toList());
    }

    /**
     * @return Only the active (as specified with the bowtie) inputs.
     */
    @Override
    public List<InputAnchor> getActiveInputs() {
        return getAllInputs().subList(0, getKnotIndex());
    }

    /** Returns the name property of this FunctionBlock. */
    public final String getName() {
        return name.get();
    }

    /** Returns the Haskell type of this FunctionBlock. */
    public final String getType() {
        return type.get();
    }

    /** Returns the bowtie index of this FunctionBlock. */
    public final Integer getKnotIndex() {
        return argumentSpace.knotIndexProperty().get();
    }

    /** Returns the StringProperty for the name of the function. */
    public final StringProperty nameProperty() {
        return name;
    }

    /** Returns the StringProperty for the type of the function. */
    public final StringProperty typeProperty() {
        return type;
    }

    /**
     * @return The current (output) expression belonging to this block.
     */
    @Override
    public final Expr asExpr() {
        Expr expr = new Ident(getName());
        for (InputAnchor in : getActiveInputs()) {
            expr = new Apply(expr, in.asExpr());
        }

        return expr;
    }

    /**
     * @return The function signature as specified in the catalog.
     */
    public Type getFunctionSignature() {
        return getPane().getEnvInstance().getFreshExprType(this.getName()).get();
    }

    public InputAnchor getInput(int index) {
        return argumentSpace.getInputArgument(index).getInputAnchor();
    }

    @Override
    public Type getInputSignature(int index) {
        return getInput(index).getSignature();
    }
    
    @Override
    public Type getInputType(int index) {
        return getInput(index).getType();
    }
    
    @Override
    public Type getOutputSignature() {
        Type type = getFunctionSignature();
        for (int i = 0; i < getKnotIndex(); i++) {
            if (type instanceof FuncT) {
                type = ((FuncT) type).getArgs()[1];
            } else {
                throw new FunctionDefinitionException();
            }
        }
        return type;
    }

    @Override
    public Type getOutputType() {
        try {
            return asExpr().analyze(getPane().getEnvInstance()).prune();
        } catch (HaskellException e) {
            return getOutputSignature();
        }
    }

    /**
     * @param index
     *            Index to specify which index to check.
     * @return Whether or not the input type connected matches its signature.
     */
    public boolean inputTypeMatches(int index) {
        try {
            HindleyMilner.unify(getInputSignature(index), getInputType(index));
            // Types successfully unified
            return true;
        } catch (HaskellTypeError e) {
            // Unable to unify types;
            return false;
        }
    }
    
    
    

    @Override
    public void invalidateConnectionState() {
        invalidateInputVisuals();
        invalidateOutputVisuals();
    }

    /**
     * Updates the input types to the Block's new state.
     */
    private void invalidateInputVisuals() {
        argumentSpace.invalidateInputContent();
    }

    /**
     * Updates the output types to the Block's new state.
     */
    private void invalidateOutputVisuals() {
        argumentSpace.invalidateOutputContent();
    }

    /**
     * React to a potential state change with regards to the bowtie index.
     * This then activates / disables the inputs with regards to the bowtie index.
     */
    private void invalidateBowtieIndex() {
        for (InputAnchor input : getAllInputs()) {
            input.setVisible(getInputIndex(input) < getKnotIndex());
            if (getInputIndex(input) >= getKnotIndex() && input.hasConnection()) {
                input.getPrimaryConnection().get().remove();
            }
        }
        ConnectionCreationManager.nextConnectionState();
        invalidateConnectionStateCascading();
    }
    
    @Override
    public final void setError(boolean error) {
        for (InputAnchor in : getAllInputs()) {
            setInputError(getInputIndex(in), error);
        }
        super.setError(error);
    }
    
    private final void setInputError(int index, boolean error) {
        argumentSpace.setInputError(index, error);
    }
    
    @Override
    public String toString() {
        return this.getName();
    }
}
