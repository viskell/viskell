package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import javafx.fxml.FXML;
import javafx.scene.shape.Shape;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.expr.LocalVar;
import nl.utwente.viskell.haskell.expr.Variable;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;

/**
 * Anchor that specifically functions as an output.
 */
public class OutputAnchor extends ConnectionAnchor {
    
    /** The visual representation of the OutputAnchor. */
    @FXML private Shape visibleAnchor;
    
    /** The invisible part of the OutputAnchor (the touch zone). */
    @FXML private Shape invisibleAnchor;

    /** The thing sticking out of an unconnected OutputAnchor. */
    @FXML private Shape openWire;
    
    /** The connections this anchor has, can be empty for no connections. */
    private List<Connection> connections;

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
    
    @Override
    public Type getType() {
        return this.binder.getBoundType();
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
     * @param addLater a mutable list of blocks that have to be added by a surrounding container
     */
    protected void extendExprGraph(LetExpression exprGraph, BlockContainer container, Set<Block> addLater) {
        boolean added = false;
        
        if (block instanceof MatchBlock) {
            added = exprGraph.addLetBinding(((MatchBlock)block).getPrimaryBinder(), block.getLocalExpr());
        } else {
            added = exprGraph.addLetBinding(binder, block.getLocalExpr());
        }
        
        if (added) {
            // for a new let binding everything from the subexpression in this block needs to be included
            block.extendExprGraph(exprGraph, container, addLater);
        }
    }
    
    /** invalidates the visual state of the block this anchor belongs to*/
    public void invalidateVisualState() {
        this.block.invalidateVisualState();
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
