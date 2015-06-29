package nl.utwente.group10.ui.components.blocks;

import com.google.common.collect.ImmutableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.ghcj.GhciSession;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;
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
        g = new InputAnchor(this, pane);
        b = new InputAnchor(this, pane);

        anchorSpace.getChildren().setAll(ImmutableList.of(r, g, b));
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

    @Override
    public Type getInputSignature(int index) {
        return new ConstT("Float");
    }

    @Override
    public Type getInputSignature(InputAnchor anchor) {
        return getInputSignature(0);
    }
}
