package nl.utwente.group10.ui.components.lines;

import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;

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

    private CustomUIPane pane;

    public Connection(CustomUIPane pane) {
        this.pane = pane;
    }

    public Connection(CustomUIPane pane, OutputAnchor from) {
        this.pane = pane;
        tryAddAnchor(from);
        setEndPosition(from.getCenterInPane());
    }

    public Connection(CustomUIPane pane, InputAnchor to) {
        this.pane = pane;
        tryAddAnchor(to);
        setStartPosition(to.getCenterInPane());
    }

    public Connection(CustomUIPane pane, OutputAnchor from, InputAnchor to) {
        this.pane = pane;
        this.setAnchor(from);
        this.setAnchor(to);
    }

    public Connection(CustomUIPane pane, InputAnchor to, OutputAnchor from) {
        this(pane, from, to);
    }

    public final CustomUIPane getPane() {
        return pane;
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
     * Shortcut to call tryAddAnchor with default settings as specified in ConnectionCreationManager.
     */
    public boolean tryAddAnchor(ConnectionAnchor anchor) {
        return tryAddAnchor(anchor,
                ConnectionCreationManager.CONNECTIONS_OVERRIDE_EXISTING,
                ConnectionCreationManager.CONNECTIONS_ALLOW_TYPE_MISMATCH);
    }

    /**
     * Tries to add an unspecified ConnectionAnchor to the connection.
     *
     * @param anchor
     *            Anchor to add
     * @param overrideExisting
     *            If set, old anchors could be disconnected to make room for
     *            this new one.
     * @param allowTypeMismatch
     *            If set, anchors can be added even though their types might
     *            mismatch.
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

    /**
     * @param anchor
     *            Anchor to get the slot of
     * @return The slot (startAnchor or endAnchor variable) this anchor should
     *         be put in if connected to this Connection
     */
    private Optional<? extends ConnectionAnchor> getAnchorSlot(ConnectionAnchor anchor) {
        if (anchor instanceof OutputAnchor) {
            return startAnchor;
        } else if (anchor instanceof InputAnchor) {
            return endAnchor;
        } else {
            return Optional.empty();
        }
    }

    /**
     * @param potentialAnchor The ConnectionAnchor to check if it matches.
     * @return Wether or not the given anchor's type unifies with the current opposite anchor.
     */
    public final boolean typesMatch(ConnectionAnchor potentialAnchor) {
        // TODO Let this return mismatch information?
        Optional<? extends ConnectionAnchor> anchor = null;
        if (potentialAnchor instanceof InputAnchor) {
            anchor = startAnchor;
        }else if(potentialAnchor instanceof OutputAnchor){
            anchor = endAnchor;
        }

        if(anchor.isPresent()){
            try {
                HindleyMilner.unify(anchor.get().getType(),
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

    /**
     * Sets an OutputAnchor or InputAnchor for this line. After setting the line will update accordingly to the possible state change.
     * @param newAnchor
     */
    private void setAnchor(ConnectionAnchor newAnchor) {
        newAnchor.addConnection(this);
        newAnchor.getBlock().layoutXProperty().addListener(this);
        newAnchor.getBlock().layoutYProperty().addListener(this);

        if (newAnchor instanceof OutputAnchor) {
            if (startAnchor.isPresent()) {
                Block start = startAnchor.get().getBlock();
                start.layoutXProperty().removeListener(this);
                start.layoutYProperty().removeListener(this);
            }
            startAnchor = Optional.of((OutputAnchor) newAnchor);
            ConnectionCreationManager.nextConnectionState();
        } else if (newAnchor instanceof InputAnchor) {
            if (endAnchor.isPresent()) {
                Block end = endAnchor.get().getBlock();
                end.layoutXProperty().removeListener(this);
                end.layoutYProperty().removeListener(this);
            }
            endAnchor = Optional.of((InputAnchor) newAnchor);
            ConnectionCreationManager.nextConnectionState();
        }

        checkError();
        updateStartEndPositions();
        invalidate(ConnectionCreationManager.getConnectionState());
    }

    /**
     * Set the endAnchor for this line. After setting the EndPosition will be
     * updated.
     *
     * @param end
     *            the InputAnchor to end at.
     */
    public void setEndAnchor(InputAnchor end) {
        setAnchor(end);
    }

    /**
     * Set the startAnchor for this line. After setting the StartPosition will
     * be updated.
     *
     * @param start
     *            The OutputAnchor to start at.
     */
    public void setStartAnchor(OutputAnchor start) {
        setAnchor(start);
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

    public final boolean isConnected() {
        return startAnchor.isPresent() && endAnchor.isPresent();
    }

    /**
     * Properly disconnects the given anchor from this Connection, notifying the anchor of its disconnection.
     */
    public final void disconnect(ConnectionAnchor anchor) {
        if (startAnchor.isPresent() && startAnchor.get().equals(anchor)) {
            startAnchor.get().disconnectConnection(this);
            startAnchor = Optional.empty();
            ConnectionCreationManager.nextConnectionState();
        }
        if (endAnchor.isPresent() && endAnchor.get().equals(anchor)) {
            endAnchor.get().disconnectConnection(this);
            endAnchor = Optional.empty();
            ConnectionCreationManager.nextConnectionState();
        }
        
        anchor.getBlock().invalidate(ConnectionCreationManager.getConnectionState());
        invalidate(ConnectionCreationManager.getConnectionState());
    }

    public final void disconnect(Optional<? extends ConnectionAnchor> anchor) {
        anchor.ifPresent(this::disconnect);
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
    
    public void invalidate(int state){
        startAnchor.ifPresent(a -> a.getBlock().invalidate(state));
        endAnchor.ifPresent(a -> a.getBlock().invalidate(state));
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
                endAnchor.get().getBlock().asExpr().analyze(getPane().getEnvInstance());
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
