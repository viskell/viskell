package nl.utwente.group10.ui.components.blocks.input;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.ghcj.GhciSession;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;

/**
 * DisplayBlock is an extension of {@link Block} that only provides a display of
 * the input it receives through it's {@link InputAnchor}. The input will be
 * rendered visually on the Block. DisplayBlock can be empty and contain no
 * value at all, the value can be altered at any time by providing a different
 * input source using a {@link Connection}.
 */
public class DisplayBlock extends Block implements InputBlock {
    /** The output String that is displayed on a Label. */
    protected StringProperty output;

    /** The Anchor that is used as input. */
    protected InputAnchor inputAnchor;

    /** The space containing the input anchor. */
    @FXML protected Pane inputSpace;

    /** The space containing the output anchor. */
    @FXML protected Pane outputSpace;
    
    /**
     * Creates a new instance of DisplayBlock.
     * @param pane
     *            The pane on which this DisplayBlock resides.
     */
    public DisplayBlock(CustomUIPane pane) {
        this(pane, "DisplayBlock");
    }
    
    protected DisplayBlock(CustomUIPane pane, String fxml) {
        super(pane);

        output = new SimpleStringProperty("New Output");

        this.loadFXML(fxml);

        inputAnchor = new InputAnchor(this);
        inputAnchor.layoutXProperty().bind(inputSpace.widthProperty().divide(2));
        inputSpace.getChildren().add(inputAnchor);        
        
        //Make sure inputSpace is drawn on top.
        BorderPane borderPane = (BorderPane) inputSpace.getParent();
        borderPane.getChildren().remove(inputSpace);
        borderPane.setTop(inputSpace);
    }

    /**
     * @return The output this Block is displaying.
     */
    public String getOutput() {
        return output.get();
    }

    /**
     * Sets the output that is displayed.
     */
    public void setOutput(final String value) {
        output.set(value);
    }

    /**
     * Property getter for the output property.
     * @return outputProperty
     */
    public StringProperty outputProperty() {
        return output;
    }

    @Override
    public void invalidateConnectionState() {
        super.invalidateConnectionState();
        try {
            Optional<GhciSession> ghci = getPane().getGhciSession();

            if (ghci.isPresent()) {
                setOutput(ghci.get().pull(inputAnchor.getExpr()));
            }
        } catch (GhciException e) {
            setOutput("???");
        }
    }
    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(inputAnchor);
    }

    @Override
    public Expr getExpr() {
        return inputAnchor.getExpr();
    }
    
    @Override
    public String toString() {
        return "DisplayBlock[" + getOutput() + "]";
    }
}
