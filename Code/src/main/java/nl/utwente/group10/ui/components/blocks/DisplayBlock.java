package nl.utwente.group10.ui.components.blocks;

import java.io.IOException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.ghcj.GhciSession;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ConnectionAnchor;
import nl.utwente.group10.ui.components.InputAnchor;

/**
 * DisplayBlock is an extension of Block that only provides a display of the
 * input it receives through it's inputAnchor. The input will be rendered
 * visually on the Block. DisplayBlock can be empty and contain no value at all,
 * the value can be altered at any time by providing a different input source
 * using a Connection.
 */
public class DisplayBlock extends Block {
    /** The Block for which this DisplayBlock displays the output. **/
    private StringProperty output;

    /** The Anchor that is used as input. */
    private InputAnchor inputAnchor;

    /** The space containing the input anchor. */
    @FXML private Pane anchorSpace;

    /** The space containing the output anchor. */
    @FXML private Pane outputSpace;

    /**
     * Creates a new instance of DisplayBlock.
     * @param pane The pane on which this DisplayBlock resides.
     * @throws IOException when the FXML definition for this block cannot be loaded.
     */
    public DisplayBlock(CustomUIPane pane) throws IOException {
        super("DisplayBlock", pane);

        output = new SimpleStringProperty("New Output");

        this.getLoader().load();

        inputAnchor = new InputAnchor(this, pane);
        anchorSpace.getChildren().add(inputAnchor);
        outputSpace.getChildren().add(this.getOutputAnchor());
    }

    /**
     * Sets the output flowing into the DisplayBlock and refresh the display.
     * @param value The value to show.
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

    public ConnectionAnchor getInputAnchor() {
        return inputAnchor;
    }

    @Override
    public final Expr asExpr() {
        return inputAnchor.asExpr();
    }

    /**
     * Invalidates the outputted value and triggers re-evaluation of the value.
     */
    public final void invalidate() {
        try {
            GhciSession ghci = GhciSession.getInstance();
            setOutput(ghci.pull(inputAnchor.asExpr()));
        } catch (GhciException e) {
            setOutput("???");
        }
    }

	public Type getInputType(InputAnchor anchor) {
		try {
			return new Ident("undefined").analyze(getPane().getEnvInstance(), new GenSet());
		} catch (HaskellException e) {
			// TODO return invalid Type?
			e.printStackTrace();
			return null;
		}
	}
}
