package nl.utwente.viskell.ui.components;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.CubicCurve;
import javafx.scene.transform.Transform;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.serialize.Bundleable;

import com.google.common.collect.ImmutableMap;


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

    /**
     * Labels for serialization to and from JSON
     */
    private static final String SOURCE_LABEL = "from";
    private static final String SINK_LABEL = "to";

    /** Starting point of this Line that can be Anchored onto other objects. */
    private final OutputAnchor startAnchor;
    /** Ending point of this Line that can be Anchored onto other objects. */
    private final InputAnchor endAnchor;

    /** Whether this connection produced an error in the latest type unification. */
    private boolean errorState;

    /** Whether this connection is impossible due to scope restrictions */
    private boolean scopeError;

    /** 
     * Construct a new Connection.
     * @param source The OutputAnchor this connection comes from
     * @param sink The InputAnchor this connection goes to
     */
    public Connection(OutputAnchor source, InputAnchor sink) {
        this.setMouseTransparent(true);
        this.setFill(null);
        
        this.startAnchor = source;
        this.endAnchor = sink;
        this.errorState = false;
        this.scopeError = false;
        
        source.getPane().addConnection(this);
        this.invalidateAnchorPositions();
        this.startAnchor.addConnection(this);
        this.startAnchor.localToSceneTransformProperty().addListener(this);
        this.endAnchor.setConnection(this);
        this.endAnchor.localToSceneTransformProperty().addListener(this);

        // typecheck the new connection to mark potential errors at the best location
        try {
            TypeChecker.unify("new connection", this.startAnchor.getType(Optional.of(this)), this.endAnchor.getType());
        } catch (HaskellTypeError e) {
            this.endAnchor.setErrorState(true);
            this.errorState = true;
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
                TypeScope scope = new TypeScope();
                TypeChecker.unify("trial connection", this.startAnchor.getType(Optional.of(this)).getFresh(scope), this.endAnchor.getType().getFresh(scope));
                // unify the actual types
                TypeChecker.unify("connection", this.startAnchor.getType(Optional.of(this)), this.endAnchor.getType());
                this.endAnchor.setErrorState(false);
                this.errorState = false;
            } catch (HaskellTypeError e) {
                this.endAnchor.setErrorState(true);
                this.errorState = true;
            }
        }

        // continue with propagating connections changes in the output anchor block 
        this.startAnchor.handleConnectionChanges(finalPhase);
    }

    /**
     * Removes this Connection, disconnecting its anchors and removing this Connection from the pane it is on.
     */
    public final void remove() {
        this.startAnchor.localToSceneTransformProperty().removeListener(this);
        this.endAnchor.localToSceneTransformProperty().removeListener(this);
        this.startAnchor.dropConnection(this);
        this.endAnchor.removeConnections();
        this.startAnchor.getPane().removeConnection(this);
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
    	this.setStartPosition(this.startAnchor.getAttachmentPoint());
    	this.setEndPosition(this.endAnchor.getAttachmentPoint());
    }

    @Override
    public String toString() {
        return "Connection connecting \n(out) " + startAnchor + "   to\n(in)  " + endAnchor;
    }

    @Override
    public Map<String, Object> toBundle() {
        ImmutableMap.Builder<String, Object> bundle = ImmutableMap.builder();
        bundle.put(SOURCE_LABEL, this.startAnchor.toBundle());
        bundle.put(SINK_LABEL, this.endAnchor.toBundle());
        return bundle.build();
    }

    public static void fromBundle(Map<String,Object> connectionBundle,
                                        Map<Integer, Block> blockLookupTable) {
        Map<String,Object> source = (Map<String,Object>)connectionBundle.get(SOURCE_LABEL);
        Integer sourceId = ((Double)source.get(ConnectionAnchor.BLOCK_LABEL)).intValue();
        Block sourceBlock = blockLookupTable.get(sourceId);
        OutputAnchor sourceAnchor = sourceBlock.getAllOutputs().get(0);

        Map<String,Object> sink = (Map<String,Object>)connectionBundle.get(SINK_LABEL);
        Integer sinkId = ((Double)sink.get(ConnectionAnchor.BLOCK_LABEL)).intValue();
        Integer sinkAnchorNumber = ((Double)sink.get(ConnectionAnchor.ANCHOR_LABEL)).intValue();
        Block sinkBlock = blockLookupTable.get(sinkId);
        InputAnchor sinkAnchor = sinkBlock.getAllInputs().get(sinkAnchorNumber);

        Connection connection = new Connection(sourceAnchor, sinkAnchor);
        connection.invalidateVisualState();
        sinkBlock.invalidateVisualState();
    }

    /**
     * Sets the start coordinates for this Connection.
     * @param point Coordinates local to this Line's parent.
     */
    private void setStartPosition(Point2D point) {
        this.setStartX(point.getX());
        this.setStartY(point.getY());
        updateBezierControlPoints(this);
        if (this.getStroke() != Color.RED) {
            this.setStroke(this.calculateColor());
        }
    }

    /**
     * Sets the end coordinates for this Connection.
     * @param point coordinates local to this Line's parent.
     */
    private void setEndPosition(Point2D point) {
        this.setEndX(point.getX());
        this.setEndY(point.getY());
        updateBezierControlPoints(this);
        if (this.getStroke() != Color.RED) {
            this.setStroke(this.calculateColor());
        }
    }

    /** Returns the current bezier offset based on the current start and end positions. */
    private static double getBezierYOffset(CubicCurve wire) {
        double distX = Math.abs(wire.getEndX() - wire.getStartX())/3;
        double diffY = wire.getEndY() - wire.getStartY();
        double distY = diffY > 0 ? diffY/2 : Math.max(0, -diffY-10); 
        if (distY < BEZIER_CONTROL_OFFSET) {
            if (distX < BEZIER_CONTROL_OFFSET) {
                // short lines are extra flexible
                return Math.max(1, Math.max(distX, distY));
            } else {
                return BEZIER_CONTROL_OFFSET;
            }
        } else {
            return Math.cbrt(distY / BEZIER_CONTROL_OFFSET) * BEZIER_CONTROL_OFFSET;
        }
    }

    /** Updates the Bezier offset (curviness) according to the current start and end positions. */
    protected static void updateBezierControlPoints(CubicCurve wire) {
        double yOffset = getBezierYOffset(wire);
        wire.setControlX1(wire.getStartX());
        wire.setControlY1(wire.getStartY() + yOffset);
        wire.setControlX2(wire.getEndX());
        wire.setControlY2(wire.getEndY() - yOffset);
    }
    
    protected static double lengthSquared(CubicCurve wire) {
        double diffX = wire.getStartX() - wire.getEndX();
        double diffY = wire.getStartY() - wire.getEndY();
        return diffX*diffX + diffY*diffY;
    }
    
    /**
     * Extends the expression graph to include all subexpression required
     * @param exprGraph the let expression representing the current expression graph
     * @param container the container to which this expression graph is constrained
     * @param outsideAnchors a mutable set of required OutputAnchors from a surrounding container
     */
    protected void extendExprGraph(LetExpression exprGraph, BlockContainer container, Set<OutputAnchor> outsideAnchors) {
        OutputAnchor anchor = this.getStartAnchor();
        if (container == anchor.getContainer())
            anchor.extendExprGraph(exprGraph, container, outsideAnchors);
        else
            outsideAnchors.add(anchor);
    }

	public void invalidateVisualState() {
	    this.scopeError = !this.endAnchor.getContainer().isContainedWithin(this.startAnchor.getContainer());
	    
		if (this.errorState) {
		    this.setStroke(Color.RED);
		    this.getStrokeDashArray().clear();
			this.setStrokeWidth(3);

		}  else if (this.scopeError) {
            this.setStroke(Color.RED);
            this.setStrokeWidth(3);
	        if (this.getStrokeDashArray().isEmpty()) {
	            this.getStrokeDashArray().addAll(10.0, 10.0);
	        }
		
		} else {
		    this.setStroke(this.calculateColor());
		    this.getStrokeDashArray().clear();
			this.setStrokeWidth(calculateTypeWidth(this.endAnchor.getType()));
		}
	}

	private Paint calculateColor() {
	    double diffX = this.getEndX() - this.getStartX();
	    double distY = Math.abs(this.getEndY() - this.getStartY());
	    long gridX = Math.abs(Math.round((this.getEndX() / 40))) % 4;
	    long gridY = Math.abs(Math.round((this.getEndY() / 60))) % 4;
	    long index = (gridX * 4 + gridY * 9) % 16;
	    double angle = Math.toDegrees(Math.atan(diffX / (distY+0.1)));
	    double hue = index * 22.5 + angle/2; 
	    double saturation = this.getEndY() > this.getStartY() ? 0.3 : 0.7;
	    double brightness = Math.min(12, distY / 40) / 100;
        return Color.hsb(hue, saturation, brightness);
    }

    private static int calculateTypeWidth(Type wireType) {
		Type type = wireType.getConcrete();
		
		int fcount = 0;
		while (type instanceof FunType) {
			fcount++;
			type = ((FunType)type).getResult();
		}
	
		if (fcount > 0) {
			return 4 + 2*fcount;
		}
		
		int arity = 0;
		while (type instanceof TypeApp) {
			arity++;
			type = ((TypeApp)type).getTypeFun();
		}
		
		if (type instanceof ListTypeCon) {
			return 5;
		}
		
		return 3 + arity;
	}

	public boolean hasTypeError() {
	    return this.errorState;
	}
	
    public boolean hasScopeError() {
        return this.scopeError;
    }

}
