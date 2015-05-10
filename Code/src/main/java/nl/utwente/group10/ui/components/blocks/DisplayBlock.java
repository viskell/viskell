package nl.utwente.group10.ui.components.blocks;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.ghcj.GhciSession;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;

/**
 * DisplayBlock is an extension of {@link Block} that only provides a display of
 * the input it receives through it's {@link InputAnchor}. The input will be
 * rendered visually on the Block. DisplayBlock can be empty and contain no
 * value at all, the value can be altered at any time by providing a different
 * input source using a {@link Connection}.
 */
public class DisplayBlock extends Block implements InputBlock {
    /** The output String to display **/
    private StringProperty output;

    /** The Anchor that is used as input. */
    private InputAnchor inputAnchor;

    /** The space containing the input anchor. */
    @FXML
    private Pane anchorSpace;

    /** The space containing the output anchor. */
    @FXML
    private Pane outputSpace;

    /**
     * Creates a new instance of DisplayBlock.
     *
     * @param pane
     *            The pane on which this DisplayBlock resides.
     */
    public DisplayBlock(CustomUIPane pane) {
        super(pane);

        output = new SimpleStringProperty("New Output");

        this.loadFXML("DisplayBlock");

        inputAnchor = new InputAnchor(this, pane);
        anchorSpace.getChildren().add(inputAnchor);
    }

    /**
     * Sets the output flowing into the DisplayBlock and refresh the display.
     *
     * @param value
     *            The value to show.
     */
    public void setOutput(final String value) {
        output.set(value);
    }

    /**
     * Returns the output value this Block has.
     * @return outputValue
     */
    public String getOutput() {
        return output.get();
    }

    /**
     * Property getter for the output property.
     * @return outputProperty
     */
    public StringProperty outputProperty() {
        return output;
    }

    @Override
    public final Expr asExpr() {
        return inputAnchor.asExpr();
    }

    /** Invalidates the outputted value and triggers re-evaluation of the value. */
    public final void invalidateConnectionState() {
        try {
            Optional<GhciSession> ghci = getPane().getGhciSession();

            if (ghci.isPresent()) {
                setOutput(ghci.get().pull(inputAnchor.asExpr()));
            }
        } catch (GhciException e) {
            setOutput("???");
        }
    }

    @Override
    public Type getInputType(InputAnchor anchor) {
        if(anchor.getPrimaryOppositeAnchor().isPresent()) {
            return anchor.getPrimaryOppositeAnchor().get().getType();
        } else {
            return getInputSignature();
        }
    }

    private Type getInputSignature() {
        // Return the type 'a', that matches anything.
        // In the future this should probably be changed to '(Show a)'
        return HindleyMilner.makeVariable();
    }

    @Override
    public Type getInputSignature(InputAnchor input) {
        return getInputSignature();
    }

    @Override
    public Type getInputSignature(int index) {
        return getInputSignature();
    }

    @Override
    public Type getInputType(int index) {
        return getInputSignature();
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(inputAnchor);
    }

    @Override
    public List<InputAnchor> getActiveInputs() {
        return getAllInputs();
    }

    @Override
    public int getInputIndex(InputAnchor anchor) {
        return 0;
    }

    public void error() {
        this.getStyleClass().add("error");
    }
}
