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
 */
public class SliderBlock extends ValueBlock {
    @FXML protected Slider slider;
    
    /** Whether this slider represent an integral value. */
    private final boolean isIntegral;
    
    /** The value as of the latest finished modification. */
    private double baseValue;

    /**
     * Constructs a new SliderBlock
     * @param pane The parent pane this Block resides on.
     * @param isIntegral wWhether this slider represent an integral value.
     */
    public SliderBlock(CustomUIPane pane, boolean isIntegral) {
        super("SliderBlock", pane, pane.getEnvInstance().buildType(isIntegral ? "Num a => a" : "Fractional a => a"));

        this.isIntegral = isIntegral;
        this.baseValue = 0;
        this.slider.setValue(0);
        this.updateValue();

        slider.valueProperty().addListener(ev -> this.updateValue());
        slider.valueChangingProperty().addListener(ev -> this.toggleSliding());
        
        slider.addEventHandler(TouchEvent.TOUCH_MOVED, event -> event.consume());
    }

    private double computeCurrentValue() {
        double offset = this.slider.getValue();
        if (this.isIntegral) {
            return Math.rint(this.baseValue + Math.signum(offset) * 7 * Math.expm1(Math.abs(offset) * 5));
        }
        else {
            return this.baseValue + Math.signum(offset) * Math.expm1(Math.abs(offset) * 7);
        }
    }
    
    private void updateValue() {
        if (this.isIntegral) {
            setValue("" + Math.round(this.computeCurrentValue()));
        } else {
            setValue(String.format(Locale.US, "%.5f", this.computeCurrentValue()));
        }
        this.initiateConnectionChanges();
    }
    
    private void toggleSliding() {
        if (!slider.isValueChanging()) {
            // on finish reset slider and use new base value
            this.baseValue = this.computeCurrentValue();
            this.slider.setValue(0.0);
        }
    }
    
    @Override
    public Optional<Block> getNewCopy() {
        SliderBlock block = new SliderBlock(this.getPane(), this.isIntegral);
        block.slider.setValue(this.slider.getValue());
        return Optional.of(block);
    }
    
}
