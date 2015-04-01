package nl.utwente.group10.ui.components;

import javafx.scene.input.MouseEvent;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.ui.CustomUIPane;

import java.io.IOException;
import java.util.Optional;

/**
 * Anchor that specifically functions as an input.
 */
public class InputAnchor extends ConnectionAnchor {
    /**
     * Optional for the Connection this anchor is connected to.
     */
    private Optional<Connection> up;

    /**
     * @param block The Block this anchor is connected to.
     * @param pane The parent pane this Anchor resides on.
     * @throws IOException when the FXML definitions cannot be loaded.
     */
    public InputAnchor(Block block, CustomUIPane pane) throws IOException {
        super(block, pane);

        up = Optional.empty();

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            // Create connection when first an ouput anchor is selected and then this input anchor gets selected.
            up.map(oldAnchor -> {
                return pane.getChildren().removeAll(oldAnchor);
            });

            pane.getLastOutputAnchor().map(anchor -> {
                try {
                    Connection upstream = new Connection(anchor, this);
                    pane.getChildren().addAll(upstream);
                    up = Optional.of(upstream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            });

            pane.invalidate();
        });
    }

    /**
     * @return The expression carried by the connection connected to this anchor.
     */
    public final Expr asExpr() {
        if (up.isPresent()) {
            return up.get().getInputBlock().asExpr();
        } else {
            return new Ident("undefined");
        }
    }
}
