package nl.utwente.group10.ui.components.blocks.output;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Value;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.exceptions.TypeUnavailableException;

/**
 * ValueBlock is an extension of Block that contains only a value and does not
 * accept input of any kind. A single output source will be generated in order
 * to connect a ValueBlock to another Block.
 * <p>
 * Extensions of ValueBlock should never accept inputs, if desired the class
 * Block should be extended instead.
 * </p>
 */
public class ValueBlock extends Block implements OutputBlock {
    /** The value of this ValueBlock. */
    private StringProperty value;

    private OutputAnchor output;

    /** The space containing the output anchor. */
    @FXML private BorderPane outputSpace;

    /**
     * @param pane
     *            The parent pane this Block resides on.
     */
    public ValueBlock(CustomUIPane pane) {
        this(pane, "ValueBlock");
    }
    
    protected ValueBlock(CustomUIPane pane, String fxml) {
        super(pane);
        value = new SimpleStringProperty("5.0");
        
        this.loadFXML(fxml);
        try {
            output = new OutputAnchor(this, new Value(new ConstT("Float"), getValue()).analyze(getPane().getEnvInstance()));
        } catch (HaskellException e) {
            throw new TypeUnavailableException();
        }

        outputSpace.setCenter(this.getOutputAnchor());
        outputSpace.toFront();
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

    /** Returns the StringProperty for the value of this ValueBlock. */
    public final StringProperty valueProperty() {
        return value;
    }

    @Override
    public Expr updateExpr() {
        return new Value(new ConstT("Float"), getValue());
    }

    @Override
    public Type getOutputType() {
        return output.getSignature();
    }
    @Override
    public Type getOutputSignature() {
        return output.getSignature();
    }

    @Override
    public OutputAnchor getOutputAnchor() {
        return output;
    }
}
