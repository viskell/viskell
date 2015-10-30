package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.transform.Transform;
import nl.utwente.viskell.ui.CustomUIPane;
import nl.utwente.viskell.ui.serialize.Bundleable;

import java.util.Map;
import java.util.Optional;


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
        ChangeListener<Transform>, Bundleable {
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
    private Connection(CustomUIPane pane) {
        this.pane = pane;
        pane.getChildren().add(0, this);
        this.errorState = new SimpleBooleanProperty(false);
        this.errorState.addListener(this::checkErrorListener);
    }

    /** 
     * Construct a new Connection.
     * @param pane The Pane this Connection is on.
     * @param anchor A ConnectionAnchor of this Connection.
     */
    public Connection(CustomUIPane pane, ConnectionAnchor anchor) {
        this(pane);
        Point2D initPos = pane.sceneToLocal(anchor.localToScene(anchor.getLocalCenter()));
        setStartPositionParent(initPos);
        setEndPositionParent(initPos);
        connectTo(anchor);
    }
    
    /** Set a new error state. */
    public void setErrorState(boolean error) {
        errorState.set(error);
    }
    
    /* GETTERS */
    
    /** @return this connection's end anchor, if any. */
    public final Optional<InputAnchor> getInputAnchor() {
        return endAnchor;
    }

    /** @return this connection's start anchor, if any. */
    public final Optional<OutputAnchor> getOutputAnchor() {
        return startAnchor;
    }

    /**
     * Get the optional output anchor on the other side of
     * the provided input anchor in this Connection.
     * 
     * @param anchor on this side of the connection  
     * @return the output anchor if it exists in this connection.
     */
    public Optional<OutputAnchor> getOppositeAnchorOf(InputAnchor anchor) {
        return this.endAnchor.filter(ia -> ia == anchor).flatMap(x -> this.startAnchor);
    }

    /**
     * Get the optional output anchor on the other side of
     * the provided input anchor in this Connection.
     * 
     * @param anchor on this side of the connection  
     * @return the input anchor if it exists in this connection.
     */
    public Optional<InputAnchor> getOppositeAnchorOf(OutputAnchor anchor) {
        return this.startAnchor.filter(oa -> oa == anchor).flatMap(x -> this.endAnchor);
    }
    
    /* SETTERS */

    /**
     * Sets an OutputAnchor or InputAnchor for this line.
     * After setting the line will update accordingly to the possible state change.
     */
    public void connectTo(ConnectionAnchor newAnchor) {
        // Add the anchor.
        if (newAnchor instanceof OutputAnchor && !startAnchor.isPresent()) {
            startAnchor = Optional.of((OutputAnchor) newAnchor);
        } else if (newAnchor instanceof InputAnchor && !endAnchor.isPresent()) {
            endAnchor = Optional.of((InputAnchor) newAnchor);
        } else {
            return;
        }
        
        // Add this to the anchor.
        newAnchor.addConnection(this);
        this.addListeners(newAnchor);
        invalidateAnchorPositions();
        
        if (this.isFullyConnected()) {
            // only when both ends are connected the visuals need to be updated
            newAnchor.handleConnectionChanges();
        }
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
        styleClass.removeAll("error");
        if (newValue) {
            styleClass.add("error");
        }
    }
    

    /* OTHER METHODS */

    /**
     * @return Whether or not both sides of this Connection are connected to an Anchor.
     */
    public final boolean isFullyConnected() {
        return startAnchor.isPresent() && endAnchor.isPresent();
    }

    /**
     * Properly disconnects the given anchor from this Connection, notifying the anchor of its disconnection.
     */
    public final void disconnect(ConnectionAnchor anchor) {
        boolean wasConnected = isFullyConnected();
        // Find out what anchor to disconnect, and do so.
        if (startAnchor.isPresent() && startAnchor.get().equals(anchor)) {
            startAnchor = Optional.empty();
        } else if (endAnchor.isPresent() && endAnchor.get().equals(anchor)) {
            endAnchor = Optional.empty();
        } else {
            return; // can't find anchor to disconnect
        }
        
        // Fully disconnect the anchor from this Connection.
        anchor.localToSceneTransformProperty().removeListener(this);
        anchor.dropConnection(this);
            
        if (wasConnected) {
            //Let the now disconnected anchor update its visuals.
            anchor.handleConnectionChanges();
            //Let the remaining connected anchors update their visuals.
            this.startAnchor.ifPresent(a -> a.handleConnectionChanges());
            this.endAnchor.ifPresent(a -> a.handleConnectionChanges());
            this.setErrorState(false);
            this.invalidateAnchorPositions();
        }
    }

    /**
     * Removes this Connection, disconnecting its anchors and removing this Connection from the pane it is on.
     */
    public final void remove() {
        startAnchor.ifPresent(a -> disconnect(a));
        endAnchor.ifPresent(a -> disconnect(a));
        pane.getChildren().remove(this);
    }

    /**
     * Adds the listeners this Connections needs to keep its visual
     * representation up-to-date to the given anchor.
     */
    private void addListeners(ConnectionAnchor anchor) {
        anchor.localToSceneTransformProperty().addListener(this);
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
        startAnchor.ifPresent(a -> setStartPositionParent(pane.sceneToLocal(a.localToScene(a.getLocalCenter()))));
        endAnchor.ifPresent(a -> setEndPositionParent(pane.sceneToLocal(a.localToScene(a.getLocalCenter()))));
    }

    @Override
    public String toString() {
        return "Connection connecting \n(out) " + startAnchor + "   to\n(in)  "
                + endAnchor;
    }

    @Override
    public Map<String, Object> toBundle() {
        ImmutableMap.Builder<String, Object> bundle = ImmutableMap.builder();

        startAnchor.ifPresent(start -> {
            Block block = start.getBlock();
            bundle.put("startBlock", block.hashCode());
            bundle.put("startAnchor", 0);
        });

        endAnchor.ifPresent(end -> {
            Block block = end.getBlock();
            bundle.put("endBlock", block.hashCode());
            bundle.put("endAnchor", block.getAllInputs().indexOf(end));
        });

        return bundle.build();
    }
}
