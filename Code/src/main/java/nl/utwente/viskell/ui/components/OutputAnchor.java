package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import nl.utwente.viskell.haskell.expr.Expression;

/**
 * Anchor that specifically functions as an output.
 */
public class OutputAnchor extends ConnectionAnchor {
    
    /** The connections this anchor has, can be empty for no connections. */
    private List<Connection> connections;

    /**
     * @param block The block this Anchor is connected to.
     */
    public OutputAnchor(Block block) {
        super(block);
        this.connections = new ArrayList<>();
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

    @Override
    public boolean canAddExtraConnection() {
        // OutputAnchors can have multiple connections;
        return true;
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
    
    /** Finish connection changes in the block this anchor belongs to. */
    public void finishConnectionChanges() {
        this.block.finishConnectionChanges();
    }

    /**
     * @return The expression carried by the block to which this anchor belongs.
     */
    @Override
    public Expression getExpr() {
        return this.block.getExpr();
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
