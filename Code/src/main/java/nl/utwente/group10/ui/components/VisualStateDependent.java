package nl.utwente.group10.ui.components;

import javafx.beans.property.IntegerProperty;

/**
 * Interface for Nodes who are dependent on their VisualState.
 * 
 * The VisualState is an integer based on the ConnectionState. VisualState
 * changes after the ConnectionState changed and when the UI should be updated.
 */
public interface VisualStateDependent {
    /** @return The VisualState the Object is in. */
    public int getVisualState();

    /** Sets the VisualState. */
    public void setVisualState(int state);
    
    /** @return the Property for the VisualState. */
    public IntegerProperty visualStateProperty();
}
