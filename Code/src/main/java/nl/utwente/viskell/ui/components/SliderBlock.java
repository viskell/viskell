package nl.utwente.viskell.ui.components;

import java.util.Locale;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.input.TouchEvent;
import nl.utwente.viskell.ui.CustomUIPane;

/**
 * An extension of ValueBlock.
 * The value of this Block can be changed by dragging a slider.
 * Ranges from 0 to 1 (both inclusive).
 */
public class SliderBlock extends ValueBlock {
    @FXML protected Slider slider;

    /**
     * Constructs a new SliderBlock
     * @param pane The parent pane this Block resides on.
     */
    public SliderBlock(CustomUIPane pane) {
        super(pane, pane.getEnvInstance().buildType("Fractional a => a"), "0.0", "SliderBlock");

        slider.setValue(0.0);
        setValue("0.0");

        slider.valueProperty().addListener(ev -> {
            setValue(String.format(Locale.US, "%.5f", slider.getValue()));
            this.initiateConnectionChanges();
        });
        
        slider.addEventHandler(TouchEvent.TOUCH_MOVED, event -> event.consume());
    }

    @Override
    public Optional<Block> getNewCopy() {
        SliderBlock block = new SliderBlock(this.getPane());
        block.slider.setValue(this.slider.getValue());
        return Optional.of(block);
    }
    
}
