package nl.utwente.group10.ui.components;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Represent an Anchor point on either a Block or a Line
 * Integers are currently the only supported data type.
 * 
 * Other data types will be supported in the future
 */
public class ConnectionAnchor extends Circle implements Initializable{

    /** Indication of what dataType this Anchor supports.*/
    public StringProperty dataType;

    /** The fxmlLoader responsible for loading the fxml of this Block.*/
    private FXMLLoader fxmlLoader;

    public ConnectionAnchor() throws IOException {
        fxmlLoader = new FXMLLoader(getClass().getResource("/ui/ConnectionAnchor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);			

        fxmlLoader.load();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    public FXMLLoader getLoader(){
        return fxmlLoader;
    }

     /**
     * @param the type of data accepted by this Anchor.
     */
    public void setDataType(String dataType) {
        this.dataType.set(dataType);
    }

    /**
     * @return the type of data accepted by this Anchor.
     */
    public String getDataType() {
        return dataType.get();
    }
    
    /**
     * the StringProperty for the data type of this Anchor.
     * @return value
     */
    public StringProperty dataTypeProperty() {
        return dataType;
    }
}
