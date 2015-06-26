package nl.utwente.group10.ui.components.anchors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.BackendUtils;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.ComponentLoader;
import nl.utwente.group10.ui.components.ConnectionStateDependent;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.output.OutputBlock;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;

/**
 * Represents an anchor of a Block that can connect to (1 or more) Connections.
 * 
 * Has an invisible part that acts as an enlargement of the touch zone.
 * 
 * The primary Connection (if present) is the first element in getConnections().
 * This means that the oldest Connection is the primary connection.
 * 
 * The ConnectionAnchor keeps track of its accepted type (signature), and will typecheck this for new connections.
 */
public abstract class ConnectionAnchor extends StackPane implements ComponentLoader, ConnectionStateDependent {

    /** The block this Anchor is connected to. */
    private Block block;

    /** The connections this anchor has, can be empty for no connections. */
    private List<Connection> connections;
    
    /** The visual representation of the ConnectionAnchor. */
    @FXML private Shape visibleAnchor;
    
    /** The invisible part of the ConnectionAnchor (the touchZone). */
    @FXML private Shape invisibleAnchor;
    
    /** The signature that is accepted by this anchor. */
    private Type signature;
    
    /** Property storing the error state. */
    private BooleanProperty isError;
    
    /** Property storing the active state. */
    private BooleanProperty active;
    
    /** The connection state this Block is in */
    private int connectionState;

    /**
     * @param block
     *            The block where this Anchor is connected to.
     * @param Type
     *            The signature that is accepted by this anchor.
     */
    public ConnectionAnchor(Block block, Type signature) {
        this.block = block;
        this.signature = signature;
        this.isError = new SimpleBooleanProperty(false);
        this.active = new SimpleBooleanProperty(true);
        active.addListener(p -> invalidateActive());
        
        this.loadFXML("ConnectionAnchor");
        connections = new ArrayList<Connection>();
    }
    
    /**
     * @return Whether or not this ConnectionAnchor is in active mode.
     */
    public boolean getActive() {
        return active.get();
    }
    
    /**
     * @param active The new active state for this ConnectionAnchor.
     */
    public void setActive(boolean active) {
        this.active.set(active);
    }
    
    /**
     * @return The property describing the active state of this ConnectionAnchor.
     */
    public BooleanProperty activeProperty() {
        return active;
    }
    
    /**
     * @return Whether or not this ConnectionAnchor is in an error state.
     */
    public boolean getIsError() {
        return isError.get();
    }
    
    /**
     * @param state The new error state for this ConnectionAnchor.
     */
    public void setIsError(boolean state) {
        isError.set(state);
    }
    
    /**
     * @return The property describing the error state of this ConnectionAnchor.
     */
    public BooleanProperty isErrorProperty() {
        return isError;
    }
    
    /**
     * @return The Shape that is the invisible part (touch zone) of this ConnectionAnchor.
     */
    public Shape getInvisibleAnchor() {
        return invisibleAnchor;
    }

    /**
     * @return Input or output type of the block associated with this anchor.
     */
    public abstract Type getType();
    
    /**
     * @return The signature as accepted by this ConnectionAnchor.
     */
    public Type getSignature() {
        return signature;
    }

    /**
     * Removes the connection from its pane, first disconnecting it from its
     * anchors.
     *
     * @param connection
     *            Connection to remove from its pane.
     */
    public void removeConnection(Connection connection) {
        if (connections.contains(connection)) {
            connections.remove(connection);
            connection.remove();
        }
    }

    /**
     * Disconnects the connection from this anchor, keeping the connection on
     * its pane.
     *
     * @param connection
     *            Connection to disconnect from.
     */
    public void disconnectConnection(Connection connection) {
        if (connections.contains(connection)) {
            connections.remove(connection);
            connection.disconnect(this);
        }
    }

    /**
     * Removes all the connections this anchor has.
     */
    public void removeConnections() {
        while (!connections.isEmpty()) {
            removeConnection(connections.get(0));
        }
    }

    /**
     * Disconnects all the connections this anchor has from this anchor.
     */
    public void disconnectConnections() {
        while (!connections.isEmpty()) {
            disconnectConnection(connections.get(0));
        }
    }

    /**
     * Adds the given connection to the connections this anchor has.
     * 
     * @param connection
     *            Connection to add
     */
    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    /** Returns true if this anchor has 1 or more connections. */
    public boolean hasConnection() {
        return !connections.isEmpty();
    }

    /**
     * @return True if the primary connection is connected.
     */
    public boolean isPrimaryConnected() {
        return isConnected(0);
    }
    
    /**
     * @return True if the primary connection is connected, and its types match.
     */
    public boolean isPrimaryConnectedCorrect() {
        return isPrimaryConnected() && BackendUtils.typesMatch(getSignature().getFresh(), getType().getFresh());
    }

    /**
     * @param index
     *            Index of the connection to check
     * @return Whether or not the connection specified by the index is connected.
     */
    public boolean isConnected(int index) {
        return index >= 0 && index < getConnections().size() && getConnections().get(index).isConnected();
    }

    /** Whether or not this anchor allows adding an extra connection. */
    public abstract boolean canAddConnection();

    /**
     * This method provides a shortcut to get the anchors on the other side of
     * the Connection from this anchor.
     * 
     * @return A list of each potential opposite anchor for each Connection this
     *         anchor has.
     */
    public List<Optional<? extends ConnectionAnchor>> getOppositeAnchors() {
        List<Optional<? extends ConnectionAnchor>> list = new ArrayList<Optional<? extends ConnectionAnchor>>();
        for (Connection c : getConnections()) {
            if (c.isConnected()) {
                if (c.getInputAnchor().isPresent() && c.getInputAnchor().get().equals(this)) {
                    list.add(c.getOutputAnchor());
                } else if (c.getOutputAnchor().isPresent() && c.getOutputAnchor().get().equals(this)) {
                    list.add(c.getInputAnchor());
                } else {
                    list.add(Optional.empty());
                }
            } else {
                list.add(Optional.empty());
            }
        }
        return list;
    }

    /**
     * @return Just the primary's opposite anchor.
     */
    public Optional<? extends ConnectionAnchor> getPrimaryOppositeAnchor() {
        if (!getOppositeAnchors().isEmpty()) {
            return getOppositeAnchors().get(0);
        } else {
            return Optional.empty();
        }
    }

    /** @return the block this anchor belongs to. */
    public final Block getBlock() {
        return block;
    }

    /** @return the connections this anchor is connected to. */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * @return The primary connection.
     */
    public Optional<Connection> getPrimaryConnection() {
        if (getConnections().size() > 0) {
            return Optional.of(getConnections().get(0));
        } else {
            return Optional.empty();
        }
    }

    /** Returns the position of the center of this anchor relative to its pane. */
    public Point2D getCenterInPane() {
        Point2D scenePos = localToScene(0, 0);
        return getPane().sceneToLocal(scenePos);
    }

    /** Returns the pane this anchor resides on. */
    public final CustomUIPane getPane() {
        return block.getPane();
    }
    
    /**
     * Reacts to a possible change in active state.
     */
    public void invalidateActive() {
        if (!active.get()) {
            removeConnections();
        }
    }
    
    @Override
    public int getConnectionState() {
        return connectionState;
    }
    
    @Override
    public void invalidateConnectionStateCascading(int state) {
        if (!connectionStateIsUpToDate(state)) {
            invalidateConnectionState();
            if (this instanceof InputAnchor) {
                getBlock().invalidateConnectionStateCascading(state);
            } else if (this instanceof OutputAnchor) {
                for (Optional<? extends ConnectionAnchor> opAnchor : getOppositeAnchors()) {
                    if (opAnchor.isPresent()) {
                        opAnchor.get().invalidateConnectionStateCascading(state);
                    }
                }
            }
            this.connectionState = state;
        }
    }
    
    @Override
    public void invalidateConnectionState() {
        setIsError(!BackendUtils.typesMatch(getSignature().getFresh(), getType().getFresh()));
    }

    @Override
    public String toString() {
        return String.format("%s for %s", this.getClass().getSimpleName(), getBlock());
    }
}
