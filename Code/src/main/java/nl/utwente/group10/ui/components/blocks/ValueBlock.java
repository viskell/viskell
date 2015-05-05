package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Value;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.ui.CustomUIPane;

/**
 * ValueBlock is an extension of Block that contains only a value and does not
 * accept input of any kind. A single output source will be generated in order
 * to connect a ValueBlock to another Block.
 * <p>
 * Extensions of ValueBlock should never accept inputs, if desired the class
 * Block should be extended instead.
 * </p>
 */
public class ValueBlock extends Block {
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

    /** Return the value of this block to be used as output. */
    public final void setValue(String value) {
        this.value.set(value);
    }

    /** Returns the value that is outputted by this Block. */
    public final String getValue() {
        return value.get();
    }

    /** the StringProperty for the value of this ValueBlock. */
    public final StringProperty valueProperty() {
        return value;
    }

    @Override
    public Expr asExpr() {
        // TODO: support more types than floats
        return new Value(new ConstT("Float"), getValue());
    }

    @Override
    public void error() {
        this.getStyleClass().add("error");
    }
}
