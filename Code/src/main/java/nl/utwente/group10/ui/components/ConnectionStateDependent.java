package nl.utwente.group10.ui.components;

import javafx.beans.property.IntegerProperty;

/**
 * Interface for Nodes who are dependent on their ConnectionState.
 * 
 * The ConnectionState is an integer controlled by the
 * ConnectionCreationManager. It changes (increments) whenever somewhere
 * something related to connections change. This could be adding a new
 * (completed) Connection, or removing an existing (complete) connection.
 */
public interface ConnectionStateDependent {
    /** @return The ConnectionState the Object is in. */
    public int getConnectionState();

    /** Sets the ConnectionState. */
    public void setConnectionState(int state);
    
    /** @return the Property for the ConnectionState. */
    public IntegerProperty connectionStateProperty();
}
