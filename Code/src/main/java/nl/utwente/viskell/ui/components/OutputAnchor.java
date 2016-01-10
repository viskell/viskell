package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.BlockContainer;

/**
 * Anchor that specifically functions as an output.
 */
public class OutputAnchor extends ConnectionAnchor implements ConnectionAnchor.Target {
    
    /** The visual representation of the OutputAnchor. */
    @FXML private Shape visibleAnchor;
    
    /** The invisible part of the OutputAnchor (the touch zone). */
    @FXML private Shape invisibleAnchor;

    /** The thing sticking out of an unconnected OutputAnchor. */
    @FXML private Shape openWire;
    
    /** The thing that is shown when this anchor is used as a boolean guard. */
    @FXML private Shape guardMarker;
    
    /** The connections this anchor has, can be empty for no connections. */
    protected List<Connection> connections;

    /** The variable binder attached to the expression corresponding to this anchor */
    protected final Binder binder;
    
    /**
     * @param block The block this Anchor is connected to
     * @param binder The binder to use for this
     */
    public OutputAnchor(Block block, Binder binder) {
        super(block);
        this.loadFXML("OutputAnchor");
        this.connections = new ArrayList<>();
        this.binder = binder;
    }
    
    /**
     * @param targetConnection optionally which special connection the type associated with.
     * @return the local type of this anchor
     */
    public Type getType(Optional<Connection> targetConnection) {
        return this.binder.getBoundType();
    }

    @Override
    public Type getFreshType() {
        return this.getType(Optional.empty()).getFresh();
    }
    
    @Override
    public ConnectionAnchor getAssociatedAnchor() {
        return this;
    }

    /**
     * @return the string representation of the in- or output type.
     */
    public final String getStringType() {
        return this.getType(Optional.empty()).prettyPrint();
    }

    /**
     * Refreshes the internal anchor type. 
     * @param scope wherein the fresh type is constructed
     */
    public void refreshType(TypeScope scope) {
        this.binder.refreshBinderType(scope);
    }
    
    /**
     * Sets the internal anchor type.
     * @param type to replace the internal type with.
     */
    public void setExactRequiredType(Type type) {
        this.binder.setAnnotationAsType(type);
    }
    
    /**
     * Set a new type constraint for this anchor, and refreshes it internal type.
     * @param type to constrain this anchor with.
     * @param scope scope wherein the fresh type is constructed.
     */
    public void setFreshRequiredType(Type type, TypeScope scope) {
        this.binder.setFreshAnnotation(type, scope);
    }
    
    /**
     * Get the input anchors on the other side of the Connection from this anchor.
     * 
     * @return A list of each input anchor for each Connection this anchor has.
     */
    public List<InputAnchor> getOppositeAnchors() {
        List<InputAnchor> list = new ArrayList<>();
        for (Connection c : this.connections) {
            list.add(c.getEndAnchor());
        }
        return list;
    }
    
    @Override
    public boolean hasConnection() {
        return !this.connections.isEmpty();
    }

    /**
     * Adds the given connection to the connections this anchor has.
     * @param connection The connection to add.
     */
    protected void addConnection(Connection connection) {
        this.connections.add(connection);
        this.openWire.setVisible(false);
        this.guardMarker.setVisible(false);
    }

    /**
     * Drops the connection from this anchor
     * @param connection Connection to disconnect from.
     */
    protected void dropConnection(Connection connection) {
        if (this.connections.contains(connection)) {
            this.connections.remove(connection);
            this.openWire.setVisible(!this.hasConnection());
        }
    }
    
    @Override
    public void removeConnections() {
        while (!this.connections.isEmpty()) {
            Connection connection = this.connections.remove(0);
            connection.remove();
        }
        this.openWire.setVisible(true);
    }

    @Override
    public Point2D getAttachmentPoint() {
        return this.getPane().sceneToLocal(this.localToScene(new Point2D(0, 7)));
    }
    
    /** Initiate connection changes at the Block this anchor is attached to. */
    public void initiateConnectionChanges() {
        this.block.initiateConnectionChanges();
    }
    
    /** Prepare connection changes in the block this anchor belongs to. */
    public void prepareConnectionChanges() {
        this.block.prepareConnectionChanges();
    }
    
    /**
     * @return The variable referring to the expression belong to this anchor.
     */
    public Variable getVariable() {
        return new LocalVar(this.binder);
    }

    /**
     * Extends the expression graph to include all subexpression required
     * @param exprGraph the let expression representing the current expression graph
     * @param container the container to which this expression graph is constrained
     * @param outsideAnchors a mutable set of required OutputAnchors from a surrounding container
     */
    protected void extendExprGraph(LetExpression exprGraph, BlockContainer container, Set<OutputAnchor> outsideAnchors) {
        if (block.getContainer().equals(container)) {
            boolean added = false;
            Expression expr = block.getLocalExpr(outsideAnchors);
            
            if (block instanceof MatchBlock) {
                added = exprGraph.addLetBinding(((MatchBlock)block).getPrimaryBinder(), expr);
            } else if (block instanceof ConstantMatchBlock) {
                added = exprGraph.addLetBinding(new ConstantBinder(((ConstantMatchBlock)block).getValue()), expr);
            } else if (block instanceof SplitterBlock) {
                added = exprGraph.addLetBinding(((SplitterBlock)block).getPrimaryBinder(), expr);
            } else {
                added = exprGraph.addLetBinding(binder, expr);
            }
            
            if (added) {
                // for a new let binding everything from the subexpression in this block needs to be included
                block.extendExprGraph(exprGraph, container, outsideAnchors);
            }
        }
    }
    
    @Override
    protected void setNearbyWireReaction(int goodness) {
        if (goodness > 0) {
            this.openWire.setStroke(Color.STEELBLUE);
            this.openWire.setStrokeWidth(5);
            this.visibleAnchor.setFill(Color.STEELBLUE);
            this.guardMarker.setStroke(Color.STEELBLUE);
        } else if (goodness < 0) {
            this.openWire.setStroke(Color.RED);
            this.openWire.setStrokeWidth(3);
            this.visibleAnchor.setFill(Color.RED);
            this.guardMarker.setStroke(Color.RED);
        } else {
            this.openWire.setStroke(Color.BLACK);
            this.openWire.setStrokeWidth(3);
            this.visibleAnchor.setFill(Color.BLACK);
            this.guardMarker.setStroke(Color.BLACK);
        }
    }

    @Override
    public void setWireInProgress(DrawWire wire) {
        super.setWireInProgress(wire);
        if (wire == null) {
            this.invalidateVisualState();
            this.invisibleAnchor.setMouseTransparent(false);
        } else {
            this.openWire.setVisible(false);
            this.guardMarker.setVisible(false);
            this.invisibleAnchor.setMouseTransparent(true);
        }
    }
    public void invalidateVisualState() {
        if ("Bool".equals(this.getStringType()) && this.getContainer() instanceof Lane) {
            this.guardMarker.setVisible(!this.hasConnection());
            this.openWire.setVisible(false);
            
        } else {
            this.openWire.setVisible(!this.hasConnection());
            this.guardMarker.setVisible(false);
        }
    }

    @Override
    public BlockContainer getContainer() {
        return this.block.getContainer();
    }
    
    @Override
    public Map<String, Object> toBundle() {
        ImmutableMap.Builder<String, Object> bundle = ImmutableMap.builder();
        Block block = this.block;
        bundle.put("endBlock", block.hashCode());
        bundle.put("endAnchor", block.getAllInputs().indexOf(this));
        return bundle.build();
    }

}
