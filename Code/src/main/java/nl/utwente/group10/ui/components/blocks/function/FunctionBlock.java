package nl.utwente.group10.ui.components.blocks.function;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.type.FunType;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.input.InputBlock;
import nl.utwente.group10.ui.components.blocks.output.OutputBlock;
import nl.utwente.group10.ui.exceptions.FunctionDefinitionException;

/**
 * Main building block for the visual interface, this class represents a Haskell
 * function together with it's arguments and visual representation.
 */
public class FunctionBlock extends Block implements InputBlock, OutputBlock {
    /** The OutputAnchor of this FunctionBlock. */
    private OutputAnchor output;

    /** The function name. */
    private StringProperty name;

    /** The space containing the input anchor(s). */
    @FXML private Pane inputSpace;

    /** The space containing the output anchor. */
    @FXML private BorderPane outputSpace;

    /** The space containing all the arguments of the function. */
    private ArgumentSpace argumentSpace;
    
    /** The space in which to nest the FunctionBlock's inner parts. */
    @FXML private Pane nestSpace;
    
    /** The space in which the information of the function is displayed. */
    @FXML private Label functionInfo;
    
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

        this.loadFXML("FunctionBlock");
        Expr signature = new Ident(getName());

        // Collect argument types
        ArrayList<String> args = new ArrayList<>();
        Type t = null;
        try {
            t = signature.getType(pane.getEnvInstance());
        } catch (HaskellException e1) {
            throw new FunctionDefinitionException();
        }
        int inputCount = 0;
        while (t instanceof FunType) {
            FunType ft = (FunType) t;
            args.add(ft.getArgument().toHaskellType());
            t = ft.getResult();
            inputCount++;
        }
        
        argumentSpace = new ArgumentSpace(this, inputCount);
        nestSpace.getChildren().add(argumentSpace);

        // Create an anchor for the result
        output = new OutputAnchor(this);
        outputSpace.setCenter(output);
        
        invalidateConnectionState();
    }

    /** @return the name of this FunctionBlock. */
    public final String getName() {
        return name.get();
    }
    
    /** @return the StringProperty for the name of the function. */
    public final StringProperty nameProperty() {
        return name;
    }

    @Override
    public final List<InputAnchor> getAllInputs() {
        return argumentSpace.getInputAnchors();
    }

    /**
     * @return Only the active (as specified by the knot index) inputs.
     */
    @Override
    public List<InputAnchor> getActiveInputs() {
        return argumentSpace.getActiveInputAnchors();
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
        expr = new Ident(getName());
        getPane().putExprToFunction(expr, this);
        
        for (InputAnchor in : getActiveInputs()) {
            expr = new Apply(expr, in.getExpr());
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

    @Override
    public String toString() {
        return this.getName();
    }
}
