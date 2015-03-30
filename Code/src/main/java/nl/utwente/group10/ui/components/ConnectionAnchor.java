package nl.utwente.group10.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.shape.Circle;
import nl.utwente.group10.ui.CustomUIPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Represent an Anchor point on either a Block or a Line
 * Integers are currently the only supported data type.
 * 
 * Other data types will be supported in the future
 */
public abstract class ConnectionAnchor extends Circle implements Initializable {
    /** The fxmlLoader responsible for loading the fxml of this Block.*/
    private FXMLLoader fxmlLoader;

    /** Our parent CustomUIPane. */
    private CustomUIPane pane;

    public ConnectionAnchor(CustomUIPane pane) throws IOException {
        this.pane = pane;

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
}
