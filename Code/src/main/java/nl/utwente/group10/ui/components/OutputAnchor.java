package nl.utwente.group10.ui.components;

import javafx.scene.input.MouseEvent;
import nl.utwente.group10.ui.CustomUIPane;

import java.io.IOException;

public class OutputAnchor extends ConnectionAnchor {
    public OutputAnchor(Block block, CustomUIPane pane) throws IOException {
        super(block, pane);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> pane.setLastOutputAnchor(this));
    }
}
