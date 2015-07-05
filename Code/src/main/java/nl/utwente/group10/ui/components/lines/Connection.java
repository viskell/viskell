package nl.utwente.group10.ui.components.lines;

import java.util.Optional;

import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.exceptions.InvalidAnchorException;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.transform.Transform;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;


/**
 * This is a Connection that represents a flow between an {@link InputAnchor}
 * and {@link OutputAnchor}. Both anchors are stored referenced respectively as
 * startAnchor and endAnchor {@link Optional} within this class.
 * <p>
 * It is possible for a connection to exist without both anchors being present,
 * whenever the position of either the start or end anchor changes the
 * {@link #invalidateAnchorPositions()} should be called to refresh the visual
 * representation of the connection.
 * </p>
 * 
 * Connection is also a changeListener for a Transform, in order to be able to
 * update the Line's position when the anchor's positions change.
 */
public class Connection extends ConnectionLine implements
        ChangeListener<Transform> {
    /** Starting point of this Line that can be Anchored onto other objects. */
    private Optional<OutputAnchor> startAnchor = Optional.empty();
    /** Ending point of this Line that can be Anchored onto other objects. */
    private Optional<InputAnchor> endAnchor = Optional.empty();

    /** The Pane this Connection is on. */
    private CustomUIPane pane;
    
    /** Property describing the error state. */
    private BooleanProperty errorState;

    /** 
     * Construct a new Connection.
     * @param pane The Pane this Connection is on.
     */
    public Connection(CustomUIPane pane) {
        this.pane = pane;
        this.errorState = new SimpleBooleanProperty(false);
        this.errorStateProperty().addListener(this::checkErrorListener);
    }

    /** 
     * Construct a new Connection.
     * @param pane The Pane this Connection is on.
     * @param from The OutputAnchor of this Connection.
     */
    public Connection(CustomUIPane pane, OutputAnchor from) {
        this(pane);
        tryAddAnchor(from);
        setEndPositionParent(getAnchorPositionInPane(from));
    }

    /** 
     * Construct a new Connection.
     * @param pane The Pane this Connection is on.
     * @param to The InputAnchor of this Connection.
     */
    public Connection(CustomUIPane pane, InputAnchor to) {
        this(pane);
        tryAddAnchor(to);
        setStartPositionParent(getAnchorPositionInPane(to));
    }

    /** 
     * Construct a new Connection.
     * @param pane The Pane this Connection is on.
     * @param from The OutputAnchor of this Connection.
     * @param to The InputAnchor of this Connection.
     */
    public Connection(CustomUIPane pane, OutputAnchor from, InputAnchor to) {
        this(pane);
        this.setAnchor(from);
        this.setAnchor(to);
    }

    /** Convenience method. */
    public Connection(CustomUIPane pane, InputAnchor to, OutputAnchor from) {
        this(pane, from, to);
    }
    
    /** @return The current error state. */
    public boolean getErrorState() {
        return errorState.get();
    }
    
    /** Set a new error state. */
    public void setErrorState(boolean error) {
        errorState.set(error);
    }
    
    /** @return The property describing the error state. */
    public BooleanProperty errorStateProperty() {
        return errorState;
    }

    
    /* GET METHODS */
    
    /**
     * @return The CustomUIPane this Conneciton is on.
     */
    public final CustomUIPane getPane() {
        return pane;
    }

    /** @return this connection's end anchor, if any. */
    public final Optional<InputAnchor> getInputAnchor() {
        return endAnchor;
    }

    /** @return the downstream block, if any. */
    public final Optional<Block> getInputBlock() {
        return endAnchor.map(ConnectionAnchor::getBlock);
    }

    /** @return this connection's start anchor, if any. */
    public final Optional<OutputAnchor> getOutputAnchor() {
        return startAnchor;
    }

    /** @return the upstream block, if any. */
    public final Optional<Block> getOutputBlock() {
        return startAnchor.map(ConnectionAnchor::getBlock);
    }
    
    /**
     * @return The position on the specified anchor local to the CustomUIPane at
     *         which the Line should start or end.
     */
    public Point2D getAnchorPositionInPane(ConnectionAnchor anchor) {
        return getPane().sceneToLocal(anchor.localToScene(anchor.getLocalCenter()));
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
    
    
    /* SET METHODS */

    /**
     * Sets an OutputAnchor or InputAnchor for this line. After setting the line
     * will update accordingly to the possible state change.
     */
    private void setAnchor(ConnectionAnchor newAnchor) {
        // Add the anchor.
        if (newAnchor instanceof OutputAnchor) {
            startAnchor.ifPresent(a -> disconnect(a));
            startAnchor = Optional.of((OutputAnchor) newAnchor);
        } else if (newAnchor instanceof InputAnchor) {
            endAnchor.ifPresent(a -> disconnect(a));
            endAnchor = Optional.of((InputAnchor) newAnchor);
        } else {
            throw new InvalidAnchorException();
        }
        
        // Add this to the anchor.
        newAnchor.addConnection(this);
        addListeners(newAnchor);

        if (isConnected()) {
            // The ConnectionState changed.
            ConnectionCreationManager.nextConnectionState();
            startAnchor.get().getBlock().setConnectionState(ConnectionCreationManager.getConnectionState());
            endAnchor.get().getBlock().setConnectionState(ConnectionCreationManager.getConnectionState());
        }
        invalidateAnchorPositions();
    }

    /**
     * Sets the free ends (empty anchors) to the specified position.
     * 
     * @param point
     *            Coordinates local to the Line's parent.
     */
    public void setFreeEnds(Point2D point) {
        if (!startAnchor.isPresent()) {
            setStartPositionParent(point);
        }
        if (!endAnchor.isPresent()) {
            setEndPositionParent(point);
        }
    }
    
    /**
     * Listener method that can be attached to a BooleanProperty in order to
     * update the error state based on that property.
     */
    private void checkErrorListener(ObservableValue<? extends Boolean> value, Boolean oldValue, Boolean newValue) {
        ObservableList<String> styleClass = this.getStyleClass();
        if (newValue) {
            styleClass.removeAll("error");
            styleClass.add("error");
        } else {
            styleClass.removeAll("error");
        }  
    }
    

    /* OTHER METHODS */

    /**
     * @return Whether or not both sides of this Connection are connected to an Anchor.
     */
    public final boolean isConnected() {
        return startAnchor.isPresent() && endAnchor.isPresent();
    }

    /**
     * Shortcut to call tryAddAnchor with default settings as specified in ConnectionCreationManager.
     */
    public boolean tryAddAnchor(ConnectionAnchor anchor) {
        return tryAddAnchor(anchor,
                ConnectionCreationManager.CONNECTIONS_OVERRIDE_EXISTING);
    }

    /**
     * Tries to add an unspecified ConnectionAnchor to the connection.
     *
     * @param anchor
     *            Anchor to add
     * @param overrideExisting
     *            If set, old anchors could be disconnected to make room for
     *            this new one.
     * @return Whether or not the anchor was added.
     */
    public boolean tryAddAnchor(ConnectionAnchor anchor, boolean overrideExisting) {
        Optional<? extends ConnectionAnchor> slot = getAnchorSlot(anchor);

        if (!slot.isPresent() || overrideExisting) {
            setAnchor(anchor);
            return true;
        }

        return false;
    }

    /**
     * Properly disconnects the given anchor from this Connection, notifying the anchor of its disconnection.
     */
    public final void disconnect(ConnectionAnchor anchor) {
        boolean disconnected = false;
        boolean wasConnected = isConnected();
        // Find out what anchor to disconnect, and do so.
        if (startAnchor.isPresent() && startAnchor.get().equals(anchor)) {
            startAnchor = Optional.empty();
            disconnected = true;
        } else if (endAnchor.isPresent() && endAnchor.get().equals(anchor)) {
            endAnchor = Optional.empty();
            disconnected = true;
        }
        
        if(disconnected) {
            // Fully disconnect the anchor from this Connection.
            removeListeners(anchor);
            anchor.disconnectConnection(this);
            
            if (wasConnected) {
                ConnectionCreationManager.nextConnectionState();
    
                //Let the now disconnected anchor update its visuals.
                anchor.getBlock().setConnectionState(ConnectionCreationManager.getConnectionState());
                //Let the remaining connected anchors update their visuals.
                startAnchor.ifPresent(a -> a.getBlock().setConnectionState(ConnectionCreationManager.getConnectionState()));
                endAnchor.ifPresent(a -> a.getBlock().setConnectionState(ConnectionCreationManager.getConnectionState()));
                this.setErrorState(false);
            }
        }
    }

    /**
     * Disconnects both anchors.
     */
    public final void disconnect() {
        startAnchor.ifPresent(a -> disconnect(a));
        endAnchor.ifPresent(a -> disconnect(a));
    }

    /**
     * Removes this Connection, disconnecting its anchors and removing this Connection from the pane it is on.
     */
    public final void remove() {
        disconnect();
        getPane().getChildren().remove(this);
    }

    /**
     * Adds the listeners this Connections needs to keep its visual
     * representation up-to-date to the given anchor.
     */
    private void addListeners(ConnectionAnchor anchor) {
        anchor.localToSceneTransformProperty().addListener(this);
    }

    /**
     * Removes the listeners this Connections needed to keep its visual
     * representation up-to-date from the given anchor.
     */
    private void removeListeners(ConnectionAnchor anchor) {
        anchor.localToSceneTransformProperty().removeListener(this);
    }

    @Override
    public final void changed(ObservableValue<? extends Transform> observable,
            Transform oldValue, Transform newValue) {
        invalidateAnchorPositions();
    }

    /**
     * Runs both the update Start end End position functions. Use when
     * refreshing UI representation of the Line.
     */
    public void invalidateAnchorPositions() {
        startAnchor.ifPresent(a -> setStartPositionParent(getPane().sceneToLocal(a.localToScene(a.getLocalCenter()))));
        endAnchor.ifPresent(a -> setEndPositionParent(getPane().sceneToLocal(a.localToScene(a.getLocalCenter()))));
    }

    @Override
    public String toString() {
        return "Connection connecting \n(out) " + startAnchor + "   to\n(in)  "
                + endAnchor;
    }
}
