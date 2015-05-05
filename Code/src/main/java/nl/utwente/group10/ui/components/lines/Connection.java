package nl.utwente.group10.ui.components.lines;

import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;

/**
 * This is a Connection that represents a flow between an {@link InputAnchor}
 * and {@link OutputAnchor}. Both anchors are stored referenced respectively as
 * startAnchor and endAnchor {@link Optional} within this class.
 * <p>
 * It is possible for a connection to exist without both anchors being present,
 * whenever the position of either the start or end anchor changes the
 * {@link #updateStartEndPositions()} should be called to refresh the visual
 * representation of the connection.
 * </p>
 */
public class Connection extends ConnectionLine implements
        ChangeListener<Number> {
    /** Starting point of this Line that can be Anchored onto other objects */
    private Optional<OutputAnchor> startAnchor = Optional.empty();
    /** Ending point of this Line that can be Anchored onto other objects */
    private Optional<InputAnchor> endAnchor = Optional.empty();

    public Connection() {
        // Allow default constructor
    }

    public Connection(OutputAnchor from) {
        this.setStartAnchor(from);
        setEndPosition(from.getCenterInPane());
    }

    public Connection(InputAnchor to) {
        this.setEndAnchor(to);
        setStartPosition(to.getCenterInPane());
    }

    public Connection(OutputAnchor from, InputAnchor to) {
        this.setStartAnchor(from);
        this.setEndAnchor(to);
    }

    public Connection(InputAnchor to, OutputAnchor from) {
        this(from, to);
    }

    /**
     * Tries to add an unspecified ConnectionAnchor to the connection.
     *
     * @param anchor
     *            Anchor to add
     * @param override
     *            If set will override (possible) existing Anchor.
     * @return Whether or not the anchor was added.
     */
    public boolean addAnchor(ConnectionAnchor anchor, boolean override) {
        boolean added = false;
        if ((!startAnchor.isPresent() || override)
                && anchor instanceof OutputAnchor) {
            disconnect(startAnchor);
            setStartAnchor((OutputAnchor) anchor);
            added = true;
        } else if ((!endAnchor.isPresent() || override)
                && anchor instanceof InputAnchor) {
            disconnect(endAnchor);
            setEndAnchor((InputAnchor) anchor);
            added = true;
        }

        return added;
    }

    public boolean addAnchor(ConnectionAnchor anchor) {
        return addAnchor(anchor, false);
    }

    private void setAnchor(Optional anchor, ConnectionAnchor newAnchor) {
        newAnchor.setConnection(this);
        newAnchor.getBlock().layoutXProperty().addListener(this);
        newAnchor.getBlock().layoutYProperty().addListener(this);

        if (newAnchor instanceof OutputAnchor) {
            if (startAnchor.isPresent()) {
                startAnchor.get().getBlock().layoutXProperty()
                        .removeListener(this);
                startAnchor.get().getBlock().layoutYProperty()
                        .removeListener(this);
            }
            startAnchor = Optional.of((OutputAnchor) newAnchor);
        } else if (newAnchor instanceof InputAnchor) {
            if (endAnchor.isPresent()) {
                endAnchor.get().getBlock().layoutXProperty()
                        .removeListener(this);
                endAnchor.get().getBlock().layoutYProperty()
                        .removeListener(this);
            }
            endAnchor = Optional.of((InputAnchor) newAnchor);
        }

        checkError();
        updateStartEndPositions();
    }

    /**
     * Set the endAnchor for this line. After setting the EndPosition will be
     * updated.
     *
     * @param end
     *            the InputAnchor to end at.
     */
    public void setEndAnchor(InputAnchor end) {
        setAnchor(endAnchor, end);
    }

    /**
     * Set the startAnchor for this line. After setting the StartPosition will
     * be updated.
     *
     * @param start
     *            The OutputAnchor to start at.
     */
    public void setStartAnchor(OutputAnchor start) {
        setAnchor(startAnchor, start);
    }

    /** Sets the free ends (empty anchors) to the specified position. */
    public void setFreeEnds(double x, double y) {
        if (!startAnchor.isPresent()) {
            setStartPosition(x, y);
        }
        if (!endAnchor.isPresent()) {
            setEndPosition(x, y);
        }
    }

    /** Returns this connection's end anchor, if any. */
    public final Optional<InputAnchor> getInputAnchor() {
        return endAnchor;
    }

    /** Returns the downstream block, if any. */
    public final Optional<Block> getInputBlock() {
        return endAnchor.map(ConnectionAnchor::getBlock);
    }

    /** Returns this connection's start anchor, if any. */
    public final Optional<OutputAnchor> getOutputAnchor() {
        return startAnchor;
    }

    /** Returns the upstream block, if any. */
    public final Optional<Block> getOutputBlock() {
        return startAnchor.map(ConnectionAnchor::getBlock);
    }

    @Override
    public final void changed(ObservableValue<? extends Number> observable,
            Number oldValue, Number newValue) {
        updateStartEndPositions();
    }

    public final void disconnect(ConnectionAnchor anchor) {
        if (startAnchor.isPresent() && startAnchor.get().equals(anchor)) {
            startAnchor.get().removeConnection(this);
            startAnchor = Optional.empty();
        }
        if (endAnchor.isPresent() && endAnchor.get().equals(anchor)) {
            endAnchor.get().removeConnection(this);
            endAnchor = Optional.empty();
        }
    }

    public final void disconnect(Optional<? extends ConnectionAnchor> anchor) {
        disconnect(anchor.orElse(null));
    }

    public final void disconnect() {
        disconnect(startAnchor);
        disconnect(endAnchor);
    }

    /**
     * Runs both the update Start end End position functions. Use when
     * refreshing UI representation of the Line.
     */
    private void updateStartEndPositions() {
        startAnchor.ifPresent(a -> setStartPosition(a.getCenterInPane()));
        endAnchor.ifPresent(a -> setEndPosition(a.getCenterInPane()));
    }

    @Override
    public String toString() {
        return "Connection connecting \n(out) " + startAnchor + "   to\n(in)  "
                + endAnchor;
    }

    /**
     * This method evaluates the validity of the created connection. If the
     * connection results in an invalid operation a visual error will be
     * displayed.
     */
    private void checkError() {
        if (startAnchor.isPresent() && endAnchor.isPresent()) {
            try {
                // TODO Obviously this will cause errors, we need a way to
                // access the Env
                endAnchor.get().getBlock().asExpr()
                        .analyze(new Env(), new GenSet());
                this.getStyleClass().remove("error");
            } catch (HaskellTypeError e) {
                this.getStyleClass().add("error");
            } catch (HaskellException e) {
                e.printStackTrace();
            }
        }
    }

    /** DEBUG METHOD trigger the error state for this Connection */
    public void error() {
        this.getStyleClass().add("error");
    }
}
