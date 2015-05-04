package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Value;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.exceptions.TypeUnavailableException;

/**
 * ValueBlock is an extension of Block that contains only a value and does not
 * accept input of any kind. A single output source will be generated in order
 * to connect a ValueBlock to another Block.
 *
 * Extensions of ValueBlock should never accept inputs, if desired the class
 * Block should be extended instead.
 */
public class ValueBlock extends Block implements OutputBlock {
    /** The value of this ValueBlock. */
    private StringProperty value;

    /** The space used for the output anchor. */
    @FXML
    private Pane outputSpace;

    /**
     * @param pane
     *            The parent pane this Block resides on.
     * @throws IOException
     *             when the FXML definition cannot be loaded.
     */
    public ValueBlock(CustomUIPane pane) throws IOException {
        super(pane);

        value = new SimpleStringProperty("5.0");

        this.getFXMLLoader("ValueBlock").load();

        outputSpace.getChildren().add(this.getOutputAnchor());
    }

    /**
     * @param value
     *            The value of this block to be used as output.
     */
    public final void setValue(String value) {
        this.value.set(value);
    }

    /**
     * Returns the value that is outputted by this Block.
     * 
     * @return output The value that is outputted by this Block.
     */
    public final String getValue() {
        return value.get();
    }

    /**
     * the StringProperty for the value of this ValueBlock.
     * 
     * @return value
     */
    public final StringProperty valueProperty() {
        return value;
    }

    @Override
    public Expr asExpr() {
        // TODO: support more types than floats
        return new Value(new ConstT("Float"), getValue());
    }

    @Override
    public Type getOutputType() {
        return getOutputType(getPane().getEnvInstance(), new GenSet());
    }

    @Override
    public Type getOutputType(Env env, GenSet genSet) {
        return getOutputSignature(env, genSet);
    }

    @Override
    public Type getOutputSignature() {
        return getOutputSignature(getPane().getEnvInstance(), new GenSet());
    }

    @Override
    public Type getOutputSignature(Env env, GenSet genSet) {
        try {
            return asExpr().analyze(env, genSet);
        } catch (HaskellException e) {
            throw new TypeUnavailableException();
        }
    }

    @Override
    public void error() {
        this.getStyleClass().add("error");
    }
}
