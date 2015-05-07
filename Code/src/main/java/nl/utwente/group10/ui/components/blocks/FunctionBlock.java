package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.BackendUtils;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.exceptions.FunctionDefinitionException;
import nl.utwente.group10.ui.exceptions.TypeUnavailableException;

/**
 * Main building block for the visual interface, this class
 * represents a Haskell function together with it's arguments and
 * visual representation.
 */
public class FunctionBlock extends Block implements InputBlock, OutputBlock {
    /** The inputs for this FunctionBlock. **/
    private List<InputAnchor> inputs;
    
    private OutputAnchor output;

    /** The function name. **/
    private StringProperty name;

    /** The type of this Function. **/
    private StringProperty type;

    @FXML private Pane anchorSpace;

    @FXML private Pane outputSpace;

    @FXML private Pane argumentSpace;

    @FXML private Pane inputTypes;

    @FXML private Pane outputTypes;

    /**
     * Method that creates a newInstance of this class along with it's visual representation
     *
     * @param name The name of the function.
     * @param type The function's type (usually a FuncT).
     * @param pane The parent pane in which this FunctionBlock exists.
     * @throws IOException when the FXML defenition for this Block cannot be loaded.
     */
    public FunctionBlock(String name, Type type, CustomUIPane pane) throws IOException {
        super(pane);

        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type.toHaskellType());

        this.getFXMLLoader("FunctionBlock").load();

        // Collect argument types
        ArrayList<String> args = new ArrayList<>();
        Type t = type;
        while (t instanceof FuncT) {
            FuncT ft = (FuncT) t;
            args.add(ft.getArgs()[0].toHaskellType());
            t = ft.getArgs()[1];
        }

        this.inputs = new ArrayList<InputAnchor>();

        // Create anchors and labels for each argument
        for (int i = 0; i < args.size(); i++) {
            inputs.add(new InputAnchor(this, pane));
            anchorSpace.getChildren().add(inputs.get(i));

            argumentSpace.getChildren().add(new Label(args.get(i)));
        }

        // Create an anchor and label for the result
        Label lbl = new Label(t.toHaskellType());
        lbl.getStyleClass().add("result");
        argumentSpace.getChildren().add(lbl);
        output = new OutputAnchor(this, pane);
        outputSpace.getChildren().add(output);
    }

    /**
     * Nest another Node object within this FunctionBlock
     * @param node The node to nest.
     */
    public final void nest(Node node) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the name property of this FunctionBlock.
     * @return name The name of this Block.
     */
    public final String getName() {
        return name.get();
    }

    /**
     * @param name The name of this FunctionBlock
     */
    public final void setName(String name) {
        this.name.set(name);
    }

    /**
     * @return type The Haskell type of this FunctionBlock.
     */
    public final String getType() {
        return type.get();
    }

    /**
     * @param type The new Haskell type for this FunctionBlock.
     */
    public final void setType(String type) {
        this.type.set(type);
    }

    /**
     * @return name The StringProperty for the name of the function.
     */
    public final StringProperty nameProperty() {
        return name;
    }

    /**
     * @return type The StringProperty for the type of the function.
     */
    public final StringProperty typeProperty() {
        return type;
    }

    /**
     * @return The array of input anchors for this function block.
     */
    @Override
    public final List<InputAnchor> getInputs() {
        return inputs;
    }

    /**
     * Returns the index of the argument matched to the Anchor.
     * @param anchor The anchor to look up.
     * @return The index of the given Anchor in the input anchor array.
     */
    @Override
    public final int getInputIndex(InputAnchor anchor) {
        return inputs.indexOf(anchor);
    }

    @Override
    public boolean inputsAreConnected() {
        return inputs.stream().allMatch(ConnectionAnchor::isConnected);
    }

    @Override
    public boolean inputIsConnected(int index) {
        return index>=0 && index < inputs.size() && inputs.get(index).isConnected();
    }

    /**
     * @return The current (output) expression belonging to this block.
     */
    @Override
    public final Expr asExpr() {
        Expr expr = new Ident(getName());
        // Find last input that is filled in
        for (InputAnchor in : getInputs()) {
            Expr inputExpr = in.asExpr();
            expr = new Apply(expr, inputExpr);
        }
        return expr;
    }

    public Type getFunctionSignature() {
        return getFunctionSignature(getPane().getEnvInstance());
    }

    public Type getFunctionSignature(Env env) {
        return env.getFreshExprType(this.getName()).get();
    }

    @Override
    public Type getInputSignature(InputAnchor input) {
        return getInputSignature(getInputIndex(input));
    }

    @Override
    public Type getInputSignature(int index) {
        if (index >= 0 && index < inputs.size()) {
            if (getFunctionSignature() instanceof ConstT) {
                return BackendUtils.dive((ConstT) getFunctionSignature(), index + 1);
            } else {
                throw new FunctionDefinitionException();
            }
        } else {
            throw new TypeUnavailableException();
        }
    }

    @Override
    public Type getInputType(InputAnchor input) {
        return getInputType(getInputIndex(input));
    }

    @Override
    public Type getInputType(int index) {
        if (getInputs().get(index).isConnected()) {
            return getInputs().get(index).getOtherAnchor().get().getType();
        } else {
            return getInputSignature(index);
        }
    }

    @Override
    public Type getOutputSignature() {
        return getOutputSignature(getPane().getEnvInstance());
    }

    @Override
    public Type getOutputSignature(Env env) {
        Type type = getFunctionSignature();
        for (int i = 0; i < getInputs().size(); i++) {
            if (type instanceof ConstT) {
                type = ((ConstT) type).getArgs()[1];
            } else {
                throw new FunctionDefinitionException();
            }
        }
        return type;
    }

    @Override
    public Type getOutputType() {
        return getOutputType(getPane().getEnvInstance());
    }

    @Override
    public Type getOutputType(Env env) {
        try {
            Type type;
            // TODO dynamically update (unify) output type with available
            // information.
            if (inputsAreConnected()) {
                type = asExpr().analyze(env).prune();
            } else {
                type = getFunctionSignature();
            }

            while (type instanceof ConstT
                    && ((ConstT) type).getArgs().length == 2) {
                type = ((ConstT) type).getArgs()[1];
            }
            return type;
        } catch (HaskellException e) {
            e.printStackTrace();
            return getOutputSignature();
        }
    }

    @Override
    public void invalidate() {
        invalidate(getPane().getEnvInstance(), new GenSet());
    }

    public void invalidate(Env env, GenSet genSet) {
        // TODO not clear and re-add all labels every invalidate()
        Type nakedType = getFunctionSignature();
        Type currentType = getOutputSignature();
        invalidateInput(env, genSet, currentType, nakedType);
        invalidateOutput();
    }

    /**
     * Updates the input types to the Block's new state.
     * 
     * @param env
     * @param genSet
     * @param outputType
     *            The current output type of the function (with inputs applied)
     * @param fullType
     *            The full type of the fuction (no inputs applied).
     */
    private void invalidateInput(Env env, GenSet genSet, Type outputType, Type fullType) {
        List<Label> labels = new ArrayList<Label>();
        for (int i = 0; i < getInputs().size(); i++) {
            labels.add(new Label(getInputType(i).toHaskellType()));
        }
        inputTypes.getChildren().setAll(labels);
    }

    private void invalidateOutput() {
        Label label = new Label(getOutputType().toHaskellType());
        outputTypes.getChildren().setAll(label);
    }

    @Override
    public final void error() {
            for (InputAnchor in : getInputs()) {
                if (!in.hasConnection()) {
                    argumentSpace.getChildren().get(getInputIndex(in)).getStyleClass().add("error");
                } else if (in.hasConnection()) {
                    argumentSpace.getChildren().get(getInputIndex(in)).getStyleClass().remove("error");
                }
            }
            this.getStyleClass().add("error");
    }

    @Override
    public OutputAnchor getOutputAnchor() {
        return output;
    }
}
