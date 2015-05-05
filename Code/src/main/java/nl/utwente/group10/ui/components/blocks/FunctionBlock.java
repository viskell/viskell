package nl.utwente.group10.ui.components.blocks;

import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
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
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;

/**
 * Main building block for the visual interface, this class represents a Haskell
 * function together with it's arguments and visual representation.
 */
public class FunctionBlock extends Block {
    /** The inputs for this FunctionBlock. **/
    private InputAnchor[] inputs;

    /** The function name. **/
    private StringProperty name;

    /** The type of this Function. **/
    private StringProperty type;

    /** The space containing the input anchor(s). */
    @FXML
    private Pane anchorSpace;

    /** The space containing the output anchor. */
    @FXML
    private Pane outputSpace;

    /** The space containing all the argument fields of the function. */
    @FXML
    private Pane argumentSpace;

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
     * Nest another {@link Node} object within this FunctionBlock
     *
     * @param node
     *            The node to nest.
     */
    public final void nest(Node node) {
        throw new UnsupportedOperationException();
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
     * Returns the index of the argument matched to the Anchor.
     *
     * @param anchor
     *            The anchor to look up.
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

    /** Returns the array of input anchors for this function block. */
    public final InputAnchor[] getInputs() {
        return inputs;
    }

    /** Returns the name property of this FunctionBlock. */
    public final String getName() {
        return name.get();
    }

    /** Returns the Haskell type of this FunctionBlock. */
    public final String getType() {
        return type.get();
    }

    /** Returns the StringProperty for the name of the function. */
    public final StringProperty nameProperty() {
        return name;
    }

    /** Returns the StringProperty for the type of the function. */
    public final StringProperty typeProperty() {
        return type;
    }

    @Override
    public final Expr asExpr() {
        Expr expr = new Ident(getName());
        for (InputAnchor in : getInputs()) {
            expr = new Apply(expr, in.asExpr());
        }

        return expr;
    }

    @Override
    public final void error() {
        for (InputAnchor in : getInputs()) {
            ObservableList<Node> children = argumentSpace.getChildren();
            Node arg = children.get(getArgumentIndex(in));

            if (!in.isConnected()) {
                arg.getStyleClass().add("error");
            } else if (in.isConnected()) {
                arg.getStyleClass().remove("error");
            }
        }
        this.getStyleClass().add("error");
    }
}
