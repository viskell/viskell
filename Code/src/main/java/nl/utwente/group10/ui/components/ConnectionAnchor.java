package nl.utwente.group10.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.shape.Circle;
import nl.utwente.group10.ui.CustomUIPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Represent an Anchor point on either a Block or a Line. Integers are currently the only supported data type. Other
 * data types will be supported in the future
 */
public abstract class ConnectionAnchor extends Circle implements Initializable {
    /** The pane on which this Anchor resides. */
    private CustomUIPane pane;

    /** The block this Anchor is connected to. */
    private Block block;

    /**
     * @param block The block where this Anchor is connected to.
     * @param pane The pane this Anchor belongs to.
     * @throws IOException when the FXML definitions cannot be loaded.
     */
    public ConnectionAnchor(Block block, CustomUIPane pane) throws IOException {
        this.block = block;
        this.pane = pane;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/ConnectionAnchor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        fxmlLoader.load();
    }

    /**
     * @return The block this anchor belongs to.
     */
    public final Block getBlock() {
        return block;
    }

    /**
     * @return The pane this anchor resides on.
     */
    public final CustomUIPane getPane() {
        return pane;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
