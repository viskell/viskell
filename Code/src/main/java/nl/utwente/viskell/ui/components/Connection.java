package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;
import javafx.scene.transform.Transform;
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
    private final OutputAnchor startAnchor;
    /** Ending point of this Line that can be Anchored onto other objects. */
    private final InputAnchor endAnchor;

    /** The Pane this Connection is on. */
    private CustomUIPane pane;
    
    /** Whether this connection produced an error in the latest type unification. */
    private boolean errorState;

    /** 
     * Construct a new Connection.
     * @param pane The Pane this Connection is on.
     * @param anchor A ConnectionAnchor of this Connection.
     */
    public Connection(CustomUIPane pane, OutputAnchor source, InputAnchor sink) {
        this.setMouseTransparent(true);
        
        this.pane = pane;
        this.startAnchor = source;
        this.endAnchor = sink;
        this.errorState = false;
        pane.addConnection(this);
        this.invalidateAnchorPositions();
        this.startAnchor.addConnection(this);
        this.startAnchor.localToSceneTransformProperty().addListener(this);
        this.endAnchor.setConnection(this);
        this.endAnchor.localToSceneTransformProperty().addListener(this);

        // typecheck the new connection to mark potential errors at the best location
        try {
            TypeChecker.unify("new connection", this.startAnchor.getType(), this.endAnchor.getType());
        } catch (HaskellTypeError e) {
            this.endAnchor.setErrorState(true);
            this.toggleErrorState(true);
        }
    }
    
    /**
     * @return the output anchor of this connection.
     */
    public OutputAnchor getStartAnchor() {
        return this.startAnchor;
    }

    /**
     * @return the input anchor of this connection.
     */
    public InputAnchor getEndAnchor() {
        return this.endAnchor;
    }
    
    /**
     * Handles the upward connections changes through an connection.
     * Also perform typechecking for this connection.
     * @param finalPhase whether the change propagation is in the second (final) phase.
     */
    public void handleConnectionChangesUpwards(boolean finalPhase) {
        // first make sure the output anchor block and types are fresh
        if (!finalPhase) {
            this.startAnchor.prepareConnectionChanges();
        }

        // for connections in error state typechecking is delayed to the final phase to keep error locations stable
        if (finalPhase == this.errorState) {
            try {
                // first a trial unification on a copy of the types to minimize error propagation
                TypeChecker.unify("trial connection", this.startAnchor.getType().getFresh(), this.endAnchor.getType().getFresh());
                // unify the actual types
                TypeChecker.unify("connection", this.startAnchor.getType(), this.endAnchor.getType());
                this.endAnchor.setErrorState(false);
                this.toggleErrorState(false);
            } catch (HaskellTypeError e) {
                this.endAnchor.setErrorState(true);
                this.toggleErrorState(true);
            }
        }

        // continue with propagating connections changes in the output anchor block 
        this.startAnchor.handleConnectionChanges(finalPhase);
    }

    /**
     * Updates the error state of this connection, including visual effects.
     * @param error The new error state
     */
    private void toggleErrorState(boolean error) {
        this.errorState = error;
        ObservableList<String> styleClass = this.getStyleClass();
        styleClass.removeAll("error");
        if (error) {
            styleClass.add("error");
        }
    }
    
    /**
     * Removes this Connection, disconnecting its anchors and removing this Connection from the pane it is on.
     */
    public final void remove() {
        this.startAnchor.localToSceneTransformProperty().removeListener(this);
        this.endAnchor.localToSceneTransformProperty().removeListener(this);
        this.startAnchor.dropConnection(this);
        this.endAnchor.removeConnections();
        this.pane.removeConnection(this);
        // propagate the connection changes of both anchors simultaneously in two phases to avoid duplicate work 
        this.startAnchor.handleConnectionChanges(false);
        this.endAnchor.handleConnectionChanges(false);
        this.startAnchor.handleConnectionChanges(true);
        this.endAnchor.handleConnectionChanges(true);
    }

    @Override
    public final void changed(ObservableValue<? extends Transform> observable, Transform oldValue, Transform newValue) {
        this.invalidateAnchorPositions();
    }

    /** Update the UI positions of both start and end anchors. */
    private void invalidateAnchorPositions() {
        this.setStartPosition(this.pane.sceneToLocal(this.startAnchor.localToScene(new Point2D(0, 0))));
        this.setEndPosition(this.pane.sceneToLocal(this.endAnchor.localToScene(new Point2D(0, 0))));
    }

    @Override
    public String toString() {
        return "Connection connecting \n(out) " + startAnchor + "   to\n(in)  " + endAnchor;
    }

    @Override
    public Map<String, Object> toBundle() {
        ImmutableMap.Builder<String, Object> bundle = ImmutableMap.builder();
        bundle.putAll(this.startAnchor.toBundle());
        bundle.putAll(this.endAnchor.toBundle());
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
