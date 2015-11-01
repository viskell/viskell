package nl.utwente.viskell.ui.components;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Hole;

/**
 * ConnectionAnchor that specifically functions as an input.
 */
public class InputAnchor extends ConnectionAnchor{
    /** The expression to return when there is no connection. */
    private Expression connectionlessExpr;
    
    /** Property storing the error state. */
    private BooleanProperty errorState;

    /**
     * @param block
     *            The Block this anchor is connected to.
     */
    public InputAnchor(Block block) {
        super(block);
        connectionlessExpr = new Hole();
        this.errorState = new SimpleBooleanProperty(false);
        this.errorState.addListener(this::checkError);
    }

    /**
     * @param state The new error state for this ConnectionAnchor.
     */
    public void setErrorState(boolean state) {
        errorState.set(state);
    }
    
    /**
     * @return The property describing the error state of this ConnectionAnchor.
     */
    public BooleanProperty errorStateProperty() {
        return errorState;
    }
    
    /**
     * @return Optional of the connection's opposite output anchor.
     */
    public Optional<OutputAnchor> getOppositeAnchor() {
        return this.getConnection(0).flatMap(c -> c.getOppositeAnchorOf(this));
    }
    
    /**
     * @return The expression carried by the connection connected to this anchor.
     */
    @Override
    public Expression getExpr() {
        return this.getOppositeAnchor().map(o -> o.getExpr()).orElse(connectionlessExpr);
    }
    
    /**
     * Gets the Expression that is connected to this, or when not connected create a fresh expression representing the open input.   
     * @return The updated expression carried by the connection connected to this anchor.
     */
    public final Expression getUpdatedExpr() {
        connectionlessExpr = new Hole();
        return getExpr();
    }

    @Override
    public boolean canAddExtraConnection() {
        // InputAnchors only support 1 connection;
        return !hasConnection();
    }

    /**
     * ChangeListener that will set the error state if isConnected().
     */
    public void checkError(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        for (Connection conn : this.getConnections()) {
            if (conn.isFullyConnected()) {
                conn.setErrorState(newValue);
            }
        }
    }

    @Override
    public String toString() {
        return "InputAnchor for " + this.block;
    }

    @Override
    public Map<String, Object> toBundle() {
        ImmutableMap.Builder<String, Object> bundle = ImmutableMap.builder();
        Block block = this.block;
        bundle.put("startBlock", block.hashCode());
        bundle.put("startAnchor", 0);
        return bundle.build();
    }

}
