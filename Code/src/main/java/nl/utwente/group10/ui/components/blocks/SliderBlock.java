package nl.utwente.group10.ui.components.blocks;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import nl.utwente.group10.ui.CustomUIPane;

import java.io.IOException;

public class SliderBlock extends ValueBlock {
    @FXML
    private Slider slider;

    /**
     * @param pane The parent pane this Block resides on.
     * @throws IOException when the FXML definition cannot be loaded.
     */
    public SliderBlock(CustomUIPane pane) throws IOException {
        super(pane, "SliderBlock");

        slider.setValue(0.0);
        this.setValue("0.0");

        slider.valueProperty().addListener(ev -> {
            setValue(String.valueOf(slider.getValue()));
            pane.invalidate();
        });
    }
}
