package nl.utwente.group10.ui.components.blocks.input;

import com.google.common.collect.ImmutableList;

import javafx.fxml.FXML;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.ghcj.GhciSession;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;

import java.util.NoSuchElementException;

public class RGBBlock extends DisplayBlock {
    private InputAnchor r;
    private InputAnchor g;
    private InputAnchor b;

    @FXML
    private Pane well;

    public RGBBlock(CustomUIPane pane) {
        super(pane, "RGBBlock");

        r = this.getAllInputs().get(0);
        g = new InputAnchor(this, HindleyMilner.makeVariable());
        b = new InputAnchor(this, HindleyMilner.makeVariable());
        //TODO: RGB block does not fully implement the InputBlock interface (it just inherits them from DisplayBlock, but that block only has 1 input instead of 3).
        
        r.layoutXProperty().bind(inputSpace.widthProperty().divide(3 / 0.5));
        g.layoutXProperty().bind(inputSpace.widthProperty().divide(3 / 1.5));
        b.layoutXProperty().bind(inputSpace.widthProperty().divide(3 / 2.5));
        
        inputSpace.getChildren().setAll(ImmutableList.of(r, g, b));
        
        //Weird hack to draw inputSpace above block content
        BorderPane borderPane = (BorderPane) inputSpace.getParent();
        borderPane.getChildren().remove(inputSpace);
        borderPane.setTop(inputSpace);

        Stop[] stops = new Stop[] { new Stop(0, Color.RED), new Stop(0.5, Color.GREEN), new Stop(1, Color.BLUE)};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        well.setBackground(new Background(new BackgroundFill(lg1, null, null)));
    }

    private int evaluate(InputAnchor anchor) {
        try {
            GhciSession ghci = getPane().getGhciSession().get();
            String result = ghci.pull(anchor.asExpr());

            double v = Math.max(0.0, Math.min(1.0, Double.valueOf(result)));
            return (int) Math.round(v * 255);
        } catch (NumberFormatException | GhciException | NoSuchElementException e) {
            return 0;
        }
    }

    public final void invalidateConnectionState() {
        super.invalidateConnectionState();

        int rv = evaluate(r);
        int gv = evaluate(g);
        int bv = evaluate(b);

        well.setBackground(new Background(new BackgroundFill(Color.rgb(rv, gv, bv), null, null)));
    }
}
