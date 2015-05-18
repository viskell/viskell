package nl.utwente.group10.ui.components.blocks;

import com.google.common.collect.ImmutableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.ghcj.GhciSession;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;

import java.util.Optional;

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
        Optional<GhciSession> ghci = getPane().getGhciSession();

        if (ghci.isPresent()) {
            try {
                String result = ghci.get().pull(anchor.asExpr());
                return (int) Math.round(Double.valueOf(result) * 255);
            } catch (NumberFormatException | GhciException e) {
                return 0;
            }
        } else {
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
