package nl.utwente.viskell.ui.components;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.ui.CustomUIPane;

public class MultiplexerBlock extends Block {
    private InputAnchor selectorAnchor;
    private OutputAnchor outputAnchor;

    @FXML private BorderPane block;

    public MultiplexerBlock(CustomUIPane pane) {
        super(pane);
        loadFXML("MultiplexerBlock");

        this.outputAnchor = new OutputAnchor(this, new Binder("res"));
        this.selectorAnchor = new InputAnchor(this);
    }

    @Override
    protected void refreshAnchorTypes() {

    }

    @Override
    public void updateExpr() {

    }

    private void redrawDecoration() {
        double w = 150;
        double h = 100;
        double taper = 20;

        // Draw a trapezoid

        Polygon polygon = new Polygon(
                70             , h / 4,
                w - 10         , h / 4,
                w - 10 - taper , h * 3 / 4,
                70 + taper     , h * 3 / 4
        );

        polygon.setFill(Color.WHITE);
        block.setCenter(polygon);

        //this.setWidth(w);
    }

    @Override
    public void invalidateVisualState() {
        redrawDecoration();
    }
}
