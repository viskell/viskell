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

    /** Our parent Block. */
    private Block block;

    public ConnectionAnchor(Block block, CustomUIPane pane) throws IOException {
        this.block = block;
        this.pane = pane;

        fxmlLoader = new FXMLLoader(getClass().getResource("/ui/ConnectionAnchor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);			

        fxmlLoader.load();
    }

    public Block getBlock() {
        return block;
    }

    public CustomUIPane getPane() {
        return pane;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    public FXMLLoader getLoader(){
        return fxmlLoader;
    }
}
