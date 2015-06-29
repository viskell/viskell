package nl.utwente.group10.ui.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.blocks.input.InputBlock;
import nl.utwente.group10.ui.components.blocks.output.OutputBlock;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;

/**
 * Interface for Nodes who are dependent on their ConnectionState.
 */
public interface ConnectionStateDependent {    
    /**
     * Tells the Node that its current state (considering connections) possibly
     * has changed.
     *
     * This method should only be called after the Node's constructor is done.
     * 
     * This method will invalidate the Node's state and react to this even if
     * the state did not change.
     */
    //public void invalidateConnectionState();
    
    /**
     * @return The ConnectionState the Node is in.
     */
    //public int getConnectionState();
    
    /**
     * Does the same as invalidateConnectionState(), but cascading to other
     * Nodes which are possibly also (indirectly) affected by the state change.
     * 
     * Cascading only happens if this Block is not up-to-date, implying that if
     * this Block is up-to-date, then so are all Blocks that are dependent on this Block.
     * 
     * @param state
     *            The newest visual state
     */
    //public void invalidateConnectionStateCascading(int state);
    
    /**
     * @return Whether or not the state of this Node confirms to the given newest state.
     */
    //default public boolean connectionStateIsUpToDate(int state) {
    //    return getConnectionState() == state;
    //}
    /**
     * Shortcut to call invalidateConnectionStateCascading(int state) with the newest state.
     */
    //default public void invalidateConnectionStateCascading() {
    //    invalidateConnectionStateCascading(ConnectionCreationManager.getConnectionState());
    //}
    
    
    
    public int getConnectionState();

    public void setConnectionState(int state);
    
    public IntegerProperty connectionStateProperty();
    
}
