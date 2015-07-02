package nl.utwente.group10.ui.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;

public interface VisualStateDependent {
    public int getVisualState();

    public void setVisualState(int state);
    
    public IntegerProperty visualStateProperty();
}
