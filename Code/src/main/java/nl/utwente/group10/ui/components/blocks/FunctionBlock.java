package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;
import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.InputAnchor;

/**
 * Main building block for the visual interface, this class
 * represents a Haskell function together with it's arguments and
 * visual representation.
 */
public class FunctionBlock extends Block {
    /** The inputs for this FunctionBlock. **/
    private InputAnchor[] inputs;

    /** The function name. **/
    private StringProperty name;

    /** The type of this Function. **/
    private StringProperty type;

    @FXML private Pane anchorSpace;

    @FXML private Pane outputSpace;

    @FXML private Pane argumentSpace;

    /**
     * Method that creates a newInstance of this class along with it's visual representation
     *
     * @param name The name of the function.
     * @param type The function's type (usually a FuncT).
     * @param pane The parent pane in which this FunctionBlock exists.
     * @throws IOException when the FXML defenition for this Block cannot be loaded.
     */
    public FunctionBlock(String name, Type type, CustomUIPane pane) throws IOException {
        super("FunctionBlock", pane);

        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type.toHaskellType());

        this.getLoader().load();

        // Collect argument types
        ArrayList<String> args = new ArrayList<>();
        Type t = type;
        while (t instanceof FuncT) {
            FuncT ft = (FuncT) t;
            args.add(ft.getArgs()[0].toHaskellType());
            t = ft.getArgs()[1];
        }

        this.inputs = new InputAnchor[args.size()];

        // Create anchors and labels for each argument
        for (int i = 0; i < args.size(); i++) {
            inputs[i] = new InputAnchor(this, pane);
            anchorSpace.getChildren().add(inputs[i]);

            argumentSpace.getChildren().add(new Label(args.get(i)));
        }

        // Create an anchor and label for the result
        Label lbl = new Label(t.toHaskellType());
        lbl.getStyleClass().add("result");
        argumentSpace.getChildren().add(lbl);
        outputSpace.getChildren().add(this.getOutputAnchor());
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
    public final InputAnchor[] getInputs() {
        return inputs;
    }

    /**
     * Returns the index of the argument matched to the Anchor.
     * @param anchor The anchor to look up.
     * @return The index of the given Anchor in the input anchor array.
     */
    public final int getArgumentIndex(ConnectionAnchor anchor) {
        int index = 0;
        /**
         * @invariant index < inputs.length
         */
        while ((inputs[index] != anchor) && (index < inputs.length)) {
            index++;
        }
        return index;
    }

    @Override
    public final Expr asExpr() {
        Expr expr = new Ident(getName());

        for (InputAnchor in : getInputs()) expr = new Apply(expr, in.asExpr());

        return expr;
    }
}
