package nl.utwente.group10.ui.components.lines;

import java.util.Optional;

import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.FunctionBlock;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;

/**
 * This is a ConnectionLine that also stores a startAnchor and an endAnchor to
 * keep track of origin points of the Line.
 *
 * For Lines that connect inputs and outputs of Blocks see Connection.
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
        tryAddAnchor(from);
        setEndPosition(from.getCenterInPane());
    }

    public Connection(InputAnchor to) {
        tryAddAnchor(to);
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
     * Sets the free ends (empty anchors) to the specified position
     */
    public void setFreeEnds(double x, double y) {
        if (!startAnchor.isPresent()) {
            setStartPosition(x, y);
        }
        if (!endAnchor.isPresent()) {
            setEndPosition(x, y);
        }
    }

    /**
     * Tries to add an unspecified ConnectionAnchor to the connection.
     *
     * @param anchor Anchor to add
     * @param override If set will override (possible) existing Anchor.
     * @return Whether or not the anchor was added.
     */
    public boolean tryAddAnchor(ConnectionAnchor anchor, boolean overrideExisting, boolean allowTypeMismatch) {
        boolean added = false;

        Optional<? extends ConnectionAnchor> slot = getAnchorSlot(anchor);

        if (!slot.isPresent() || overrideExisting) {
            disconnect(slot);
            boolean typesMatch = typesMatch(anchor);
            if (typesMatch || allowTypeMismatch) {
                setAnchor(anchor);
                added = true;
            }
            if (!typesMatch) {
                // TODO type mismatch
                System.out.println("Type mismatch!");
            }
        }
        updateStartEndPositions();

        return added;
    }
    
    private Optional<? extends ConnectionAnchor> getAnchorSlot(ConnectionAnchor anchor){
        if(anchor instanceof OutputAnchor){
            return startAnchor;
        } else if(anchor instanceof InputAnchor){
            return endAnchor;
        } else {
            return Optional.empty();
        }
    }

    public boolean tryAddAnchor(ConnectionAnchor anchor) {
        return tryAddAnchor(anchor,
                ConnectionCreationManager.CONNECTIONS_OVERRIDE_EXISTING,
                ConnectionCreationManager.CONNECTIONS_ALLOW_TYPE_MISMATCH);
    }

    public final boolean typesMatch(ConnectionAnchor potentialAnchor) {
        // TODO Let this return mismatch information?
        if (potentialAnchor instanceof InputAnchor) {
            if (startAnchor.isPresent()) {
                try {
                    HindleyMilner.unify(startAnchor.get().getType(),
                            potentialAnchor.getType());
                    // Types successfully unified
                    return true;
                } catch (HaskellTypeError e) {
                    // Unable to unify types;
                    return false;
                }
            } else {
                // First anchor to be added
                return true;
            }
        } else if (potentialAnchor instanceof OutputAnchor) {
            if (endAnchor.isPresent()) {
                try {
                    HindleyMilner.unify(endAnchor.get().getType(),
                            potentialAnchor.getType());
                    // Types successfully unified
                    return true;
                } catch (HaskellTypeError e) {
                    // Unable to unify types;
                    return false;
                }
            } else {
                // First anchor to be added
                return true;
            }
        }
        return false;
    }

    /**
     * Set the startAnchor for this line. After setting the StartPosition will
     * be updated.
     *
     * @param start The OutputAnchor to start at.
     */
    public void setStartAnchor(OutputAnchor start) {
        setAnchor(start);
    }

    /**
     * Set the endAnchor for this line. After setting the EndPosition will be
     * updated.
     *
     * @param end the InputAnchor to end at.
     */
    public void setEndAnchor(InputAnchor end) {
        setAnchor(end);
    }

    private void setAnchor(ConnectionAnchor newAnchor) {
        newAnchor.setConnection(this);
        newAnchor.getBlock().layoutXProperty().addListener(this);
        newAnchor.getBlock().layoutYProperty().addListener(this);

        if (newAnchor instanceof OutputAnchor) {
            if (startAnchor.isPresent()) {
                startAnchor.get().getBlock().layoutXProperty().removeListener(this);
                startAnchor.get().getBlock().layoutYProperty().removeListener(this);
            }
            startAnchor = Optional.of((OutputAnchor)newAnchor);
        } else if (newAnchor instanceof InputAnchor) {
            if (endAnchor.isPresent()) {
                endAnchor.get().getBlock().layoutXProperty().removeListener(this);
                endAnchor.get().getBlock().layoutYProperty().removeListener(this);
            }
            endAnchor = Optional.of((InputAnchor)newAnchor);
        }

        checkError();
        updateStartEndPositions();
    }

    /**
     * Runs both the update Start end End position functions. Use when
     * refreshing UI representation of the Line.
     */
    private void updateStartEndPositions() {
        startAnchor.ifPresent(a -> setStartPosition(a.getCenterInPane()));
        endAnchor.ifPresent(a -> setEndPosition(a.getCenterInPane()));
    }

    /** @return this connection's start anchor, if any. */
    public final Optional<OutputAnchor> getOutputAnchor() {
        return startAnchor;
    }

    /** @return the upstream block, if any. */
    public final Optional<Block> getOutputBlock() {
        return startAnchor.map(ConnectionAnchor::getBlock);
    }

    /** @return this connection's end anchor, if any. */
    public final Optional<InputAnchor> getInputAnchor() {
        return endAnchor;
    }

    /** @return the downstream block, if any. */
    public final Optional<Block> getInputBlock() {
        return endAnchor.map(ConnectionAnchor::getBlock);
    }

    @Override
    public final void changed(ObservableValue<? extends Number> observable,
            Number oldValue, Number newValue) {
        updateStartEndPositions();
    }

    public final boolean isConnected() {
        return startAnchor.isPresent() && endAnchor.isPresent();
    }

    public final void disconnect(ConnectionAnchor anchor) {
        if (startAnchor.isPresent() && startAnchor.get().equals(anchor)) {
            startAnchor.get().disconnectFrom(this);
            startAnchor = Optional.empty();
        }
        if (endAnchor.isPresent() && endAnchor.get().equals(anchor)) {
            endAnchor.get().disconnectFrom(this);
            endAnchor = Optional.empty();
        }
    }

    public final void disconnect(Optional<? extends ConnectionAnchor> anchor) {
        anchor.ifPresent(this::disconnect);
    }

    public final void disconnect() {
        disconnect(startAnchor);
        disconnect(endAnchor);
    }

    @Override
    public String toString() {
        return "Connection connecting \n(out) " + startAnchor + "   to\n(in)  "
                + endAnchor;
    }

    /**
     * This method evaluates the validity of the created connection.
     * If the connection results in an invalid operation a visual
     * error will be displayed.
     */
    private void checkError() {
        if(startAnchor.isPresent() && endAnchor.isPresent()) {
            try {
                //TODO Obviously this will cause errors, we need a way to access the Env
                endAnchor.get().getBlock().asExpr().analyze(new Env(), new GenSet());
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
