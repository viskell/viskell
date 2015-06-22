package nl.utwente.group10.ui.components.lines;

import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.Type;
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
 * {@link #invalidateConnectionState()} should be called to refresh the visual
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

    /** The connection state this Connection is in */
    private int connectionState;

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

        if (overrideExisting || !slot.isPresent()) {
            slot.ifPresent(a -> disconnect(a));
            // TODO since invalidateConnectionState(), and thus checkError()
            // gets called after this typeMatch(), a small duplication of
            // type getting occurs.
            if (allowTypeMismatch || typesMatch(anchor)) {
                setAnchor(anchor);
                added = true;
            }
        }
        invalidateConnectionState();

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


    public final boolean typesMatch() {
        // TODO Let this return mismatch information?
        return !isConnected() || typesMatch(startAnchor.get().getType(), endAnchor.get().getSignature());
    }
    
    /**
     * @param potentialAnchor The ConnectionAnchor to check if it matches.
     * @return Wether or not the given anchor's type unifies with the current opposite anchor.
     */
    public final boolean typesMatch(ConnectionAnchor potentialAnchor) {
        if (potentialAnchor instanceof InputAnchor && startAnchor.isPresent()) {
            return typesMatch(startAnchor.get().getType(), ((InputAnchor) potentialAnchor).getSignature());
        } else if (potentialAnchor instanceof OutputAnchor && endAnchor.isPresent()) {
            return typesMatch(endAnchor.get().getSignature(), potentialAnchor.getType());
        } else {
            return false;
        }
    }

    public final boolean typesMatch(Type t1, Type t2) {
        try {
            HindleyMilner.unify(t1, t2);
            // Types successfully unified
            return true;
        } catch (HaskellTypeError e) {
            // Unable to unify types;
            return false;
        }
    }

    /**
     * Sets an OutputAnchor or InputAnchor for this line. After setting the line will update accordingly to the possible state change.
     * @param newAnchor
     */
    private void setAnchor(ConnectionAnchor newAnchor) {
        newAnchor.addConnection(this);
        addListeners(newAnchor);

        if (newAnchor instanceof OutputAnchor) {
            startAnchor.ifPresent(a -> removeListeners(a));
            startAnchor = Optional.of((OutputAnchor) newAnchor);
        } else if (newAnchor instanceof InputAnchor) {
            endAnchor.ifPresent(a -> removeListeners(a));
            endAnchor = Optional.of((InputAnchor) newAnchor);
        }

        ConnectionCreationManager.nextConnectionState();
        invalidateConnectionStateCascading();
    }

    /**
     * Adds the listeners this Connections needs to keep its visual
     * representation up-to-date to the given anchor.
     */
    private void addListeners(ConnectionAnchor anchor) {
        anchor.layoutXProperty().addListener(this);
        anchor.layoutYProperty().addListener(this);
        anchor.getBlock().layoutXProperty().addListener(this);
        anchor.getBlock().layoutYProperty().addListener(this);
    }

    /**
     * Removes the listeners this Connections needed to keep its visual
     * representation up-to-date from the given anchor.
     */
    private void removeListeners(ConnectionAnchor anchor) {
        anchor.layoutXProperty().removeListener(this);
        anchor.layoutYProperty().removeListener(this);
        anchor.getBlock().layoutXProperty().removeListener(this);
        anchor.getBlock().layoutYProperty().removeListener(this);
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
        invalidateAnchorPositions();
    }

    /**
     * @return Whether or not both sides of this Connection are connected to an Anchor.
     */
    public final boolean isConnected() {
        return startAnchor.isPresent() && endAnchor.isPresent();
    }

    /**
     * Removes this Connection, disconnecting its anchors and removing this Connection from the pane it is on.
     */
    public final void remove() {
        disconnect();
        getPane().getChildren().remove(this);
    }

    /**
     * Properly disconnects the given anchor from this Connection, notifying the anchor of its disconnection.
     */
    public final void disconnect(ConnectionAnchor anchor) {
        if (startAnchor.isPresent() && startAnchor.get().equals(anchor)) {
            startAnchor = Optional.empty();
            anchor.disconnectConnection(this);
            ConnectionCreationManager.nextConnectionState();
        }
        if (endAnchor.isPresent() && endAnchor.get().equals(anchor)) {
            endAnchor = Optional.empty();
            anchor.disconnectConnection(this);
            ConnectionCreationManager.nextConnectionState();
        }

        //Let the now (potentially) disconnected block update its visuals.
        anchor.getBlock().invalidateConnectionStateCascading();
        //Let the remaining connected anchors update their visuals.
        invalidateConnectionStateCascading();
    }

    /**
     * Disconnects both anchors.
     */
    public final void disconnect() {
        startAnchor.ifPresent(a -> disconnect(a));
        endAnchor.ifPresent(a -> disconnect(a));
    }

    /**
     * Runs both the update Start end End position functions. Use when
     * refreshing UI representation of the Line.
     */
    public void invalidateAnchorPositions() {
        startAnchor.ifPresent(a -> setStartPosition(a.getCenterInPane()));
        endAnchor.ifPresent(a -> setEndPosition(a.getCenterInPane()));
    }
    
    /**
     * Tells the Connection that its current state (considering connections) possibly has
     * changed.
     *
     * This method should only be called after the Connection's constructor is done.
     * 
     * This method will invalidate the Connection even if the state did not change.
     */
    private void invalidateConnectionState() {
        invalidateAnchorPositions();
        checkError();
    }

    /**
     * Does the same as invalidateConnectionVisuals(), but cascading down to
     * other blocks which are possibly also (indirectly) affected by the state
     * change.
     *
     * @param state
     *            The newest visual state
     */
    public void invalidateConnectionStateCascading(int state) {
        if (!connectionStateIsUpToDate(state)) {
            invalidateConnectionState();
            startAnchor.ifPresent(a -> a.getBlock().invalidateConnectionStateCascading(state));
            endAnchor.ifPresent(a -> a.getBlock().invalidateConnectionStateCascading(state));
            this.connectionState = state;
        }
    }

    /**
     * Shortcut to call invalidateConnectionVisualsCascading(int state) with the newest state.
     */
    public void invalidateConnectionStateCascading() {
        invalidateConnectionStateCascading(ConnectionCreationManager.getConnectionState());
    }
    
    /**
     * @return Whether or not the state of the block confirms to the given newest state.
     */
    public boolean connectionStateIsUpToDate(int state) {
        return this.connectionState == state;
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
        //setError(!typesMatch());
    }
    
    public void setError(boolean error) {
        if (error) {
            this.getStyleClass().add("error");
        } else {
            this.getStyleClass().removeAll("error");
        }
    }
}
