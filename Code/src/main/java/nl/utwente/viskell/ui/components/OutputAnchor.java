package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.expr.LocalVar;
import nl.utwente.viskell.haskell.expr.Variable;

/**
 * Anchor that specifically functions as an output.
 */
public class OutputAnchor extends ConnectionAnchor {
    
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
        this.connections = new ArrayList<>();
        this.binder = binder;
        // By default the invisible anchor covers an area above the visible
        // anchor (for InputAnchors), this switches that around to cover more of
        // the area under the visible anchor.
        getInvisibleAnchor().setTranslateY(getInvisibleAnchor().getTranslateY() * -1);
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
    }

    /**
     * Drops the connection from this anchor
     * @param connection Connection to disconnect from.
     */
    protected void dropConnection(Connection connection) {
        if (this.connections.contains(connection)) {
            this.connections.remove(connection);
        }
    }
    
    @Override
    public void removeConnections() {
        while (!this.connections.isEmpty()) {
            Connection connection = this.connections.remove(0);
            connection.remove();
        }
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
     */
    protected void extendExprGraph(LetExpression exprGraph) {
        boolean added = exprGraph.addLetBinding(this.binder, this.block.getLocalExpr());
        if (added) {
            // for a new let binding everything from the subexpression in this block needs to be included
            this.block.extendExprGraph(exprGraph);
        }
    }
    
    /** invalidates the visual state of the block this anchor belongs to*/
    public void invalidateVisualState() {
        this.block.staleVisuals.set(true);
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
