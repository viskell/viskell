package nl.utwente.group10.ui.components.blocks.output;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;

/**
 * An extension of ValueBlock.
 * The value of this Block can be changed by dragging a slider.
 * Ranges from 0 to 1 (both inclusive).
 */
public class SliderBlock extends ValueBlock {
    @FXML private Slider slider;

    /**
     * Constructs a new SliderBlock
     * @param pane The parent pane this Block resides on.
     */
    public SliderBlock(CustomUIPane pane) {
        super(pane, new ConstT("Float"), "0.0", "SliderBlock");

        slider.setValue(0.0);
        this.setValue("0.0");

        slider.valueProperty().addListener(ev -> {
            setValue(String.valueOf(slider.getValue()));
            this.setConnectionState(ConnectionCreationManager.nextConnectionState());
        });
    }
}
