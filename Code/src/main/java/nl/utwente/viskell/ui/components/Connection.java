package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;
import javafx.scene.transform.Transform;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.TypeChecker;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.CustomUIPane;
import nl.utwente.viskell.ui.serialize.Bundleable;

import java.util.Map;
import java.util.Optional;


/**
 * This is a Connection that represents a flow between an {@link InputAnchor}
 * and {@link OutputAnchor}. Both anchors are stored referenced respectively as
 * startAnchor and endAnchor {@link Optional} within this class.
 * Visually a connection is represented as a cubic Bezier curve.
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
public class Connection extends CubicCurve implements
        ChangeListener<Transform>, Bundleable, ComponentLoader {
    
    /**
     * Control offset for this bezier curve of this line.
     * It determines how a far a line attempts to goes straight from its end points.
     */
    public static final double BEZIER_CONTROL_OFFSET = 150f;
    
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
     * @param anchor A ConnectionAnchor of this Connection.
     */
    public Connection(CustomUIPane pane, ConnectionAnchor anchor) {
        this.loadFXML("Connection");
        TactilePane.setDraggable(this, false);
        TactilePane.setGoToForegroundOnContact(this, false);
        this.setMouseTransparent(true);
        
        this.pane = pane;
        pane.getChildren().add(0, this);
        this.errorState = new SimpleBooleanProperty(false);
        this.errorState.addListener(this::checkErrorListener);
        Point2D initPos = pane.sceneToLocal(anchor.localToScene(anchor.getLocalCenter()));
        this.setStartPosition(initPos);
        this.setEndPosition(initPos);
        connectTo(anchor);
    }
    
    /** Set a new error state. */
    public void setErrorState(boolean error) {
        errorState.set(error);
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
    
    /**
     * Handles the upward connections changes through an connection.
     * Also perform typechecking for this connection.
     * @param input the input anchor of this change propagation.
     */
    public void handleConnectionChangesFrom(InputAnchor input) {
        if (!this.startAnchor.isPresent()) {
            return;
        }
        
        OutputAnchor output = this.startAnchor.get();
        // first make sure the output is up to date
        output.handleConnectionChanges();

        // for connections in error state typechecking is delayed to keep error locations stable
        if (this.errorState.get()) {
            return;
        }

        try {
            // first a trial unification on a copy of the types to minimize error propagation
            TypeChecker.unify("trial connection", output.getType().getFresh(), input.getType().getFresh());
            // unify the actual types
            TypeChecker.unify("connection", output.getType(), input.getType());
        } catch (HaskellTypeError e) {
            input.setErrorState(true);
        }
    }

    public Optional<Expression> getExprFrom(InputAnchor input){
        if (!this.startAnchor.isPresent()) {
            return Optional.empty();
        }
        
        OutputAnchor output = this.startAnchor.get();
        
        if (this.errorState.get()) {
            // attempt to recover from an error
            try {
                // first a trial unification on a copy of the types to minimize error propagation
                TypeChecker.unify("trial error recovery", output.getType().getFresh(), input.getType().getFresh());
                // unify the actual types
                TypeChecker.unify("error recovery", output.getType(), input.getType());
                input.setErrorState(false);
            } catch (HaskellTypeError e) {
                // the error is still present
            }
        }
        
        return Optional.of(output.getExpr());
    }
    
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
        newAnchor.localToSceneTransformProperty().addListener(this);
        invalidateAnchorPositions();
        
        // only when both ends are connected the visuals need to be updated
        if (this.isFullyConnected()) {
            // typecheck the new connection to mark potential errors at the best location
            try {
                TypeChecker.unify("new connection", this.startAnchor.get().getType(), this.endAnchor.get().getType());
            } catch (HaskellTypeError e) {
                this.endAnchor.get().setErrorState(true);
            }

            newAnchor.handleConnectionChanges();
        }
    }

    /**
     * Sets the free ends (empty anchors) to the specified position.
     * 
     * @param point Coordinates local to the Line's parent.
     */
    public void setFreeEnds(Point2D point) {
        if (!startAnchor.isPresent()) {
            this.setStartPosition(point);
        }
        if (!endAnchor.isPresent()) {
            this.setEndPosition(point);
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

    @Override
    public final void changed(ObservableValue<? extends Transform> observable, Transform oldValue, Transform newValue) {
        this.invalidateAnchorPositions();
    }

    /** Update the UI positions of both start and end anchors. */
    private void invalidateAnchorPositions() {
        startAnchor.ifPresent(a -> this.setStartPosition(pane.sceneToLocal(a.localToScene(a.getLocalCenter()))));
        endAnchor.ifPresent(a -> this.setEndPosition(pane.sceneToLocal(a.localToScene(a.getLocalCenter()))));
    }

    @Override
    public String toString() {
        return "Connection connecting \n(out) " + startAnchor + "   to\n(in)  " + endAnchor;
    }

    @Override
    public Map<String, Object> toBundle() {
        ImmutableMap.Builder<String, Object> bundle = ImmutableMap.builder();
        startAnchor.ifPresent(start -> bundle.putAll(start.toBundle()));
        endAnchor.ifPresent(end -> bundle.putAll(end.toBundle()));
        return bundle.build();
    }

    /**
     * Sets the start coordinates for this Connection.
     * @param point Coordinates local to this Line's parent.
     */
    private void setStartPosition(Point2D point) {
        this.setStartX(point.getX());
        this.setStartY(point.getY());
        this.updateBezierControlPoints();
    }

    /**
     * Sets the end coordinates for this Connection.
     * @param point coordinates local to this Line's parent.
     */
    private void setEndPosition(Point2D point) {
        this.setEndX(point.getX());
        this.setEndY(point.getY());
        this.updateBezierControlPoints();
    }

    /** Returns the current bezier offset based on the current start and end positions. */
    private double getBezierYOffset() {
        double distX = Math.abs(this.getEndX() - this.getStartX());
        double distY = Math.abs(this.getEndY() - this.getStartY());
        if (distY < BEZIER_CONTROL_OFFSET) {
            if (distX < BEZIER_CONTROL_OFFSET) {
                // short lines are extra flexible
                return Math.max(BEZIER_CONTROL_OFFSET/10, Math.max(distX, distY));
            } else {
                return BEZIER_CONTROL_OFFSET;
            }
        } else {
            return Math.cbrt(distY / BEZIER_CONTROL_OFFSET) * BEZIER_CONTROL_OFFSET;
        }
    }

    /** Updates the Bezier offset (curviness) according to the current start and end positions. */
    private void updateBezierControlPoints() {
        double yOffset = this.getBezierYOffset();
        this.setControlX1(this.getStartX());
        this.setControlY1(this.getStartY() + yOffset);
        this.setControlX2(this.getEndX());
        this.setControlY2(this.getEndY() - yOffset);
    }
}
