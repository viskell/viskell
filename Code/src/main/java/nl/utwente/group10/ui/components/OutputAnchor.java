package nl.utwente.group10.ui.components;

import javafx.scene.input.MouseEvent;
import nl.utwente.group10.ui.CustomUIPane;

import java.io.IOException;

/**
 * Anchor that specifically functions as an output.
 */
public class OutputAnchor extends ConnectionAnchor {
    /**
     * @param block The block this Anchor is connected to.
     * @param pane The parent pane on which this anchor resides.
     * @throws IOException when the FXML definition of this anchor cannot be loaded.
     */
    public OutputAnchor(Block block, CustomUIPane pane) throws IOException {
        super(block, pane);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> pane.setLastOutputAnchor(this));
    }
}
