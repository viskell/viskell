package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.ui.CustomUIPane;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Block with three inputs that represent RGB values.
 * The RGBBlock colors according to these inputs.
 */
public class RGBBlock extends DisplayBlock {
    /** The InputAnchor to which the Red value should be connected. **/
    private final InputAnchor r;
    
    /** The InputAnchor to which the Green value should be connected. **/
    private final InputAnchor g;
    
    /** The InputAnchor to which the Blue value should be connected. **/
    private final InputAnchor b;

    /** The Pane to paint with the currently inputed color. **/
    @FXML private Pane well;
    
    /** Default Background to use when no values are inputed. **/
    private final Background defaultBackground;

    public RGBBlock(CustomUIPane pane) {
        super(pane, "RGBBlock");

        r = super.getAllInputs().get(0);
        g = new InputAnchor(this);
        b = new InputAnchor(this);
        inputSpace.getChildren().setAll(ImmutableList.of(r, g, b));
        
        // Spread them out over the top of the Block.
        r.layoutXProperty().bind(inputSpace.widthProperty().divide(3 / 0.5));
        g.layoutXProperty().bind(inputSpace.widthProperty().divide(3 / 1.5));
        b.layoutXProperty().bind(inputSpace.widthProperty().divide(3 / 2.5));
        
        //Make sure inputSpace is drawn on top.
        BorderPane borderPane = (BorderPane) inputSpace.getParent();
        borderPane.getChildren().remove(inputSpace);
        borderPane.setTop(inputSpace);

        //Makes the default background.
        Stop[] stops = new Stop[] { new Stop(0, Color.RED), new Stop(0.5, Color.GREEN), new Stop(1, Color.BLUE)};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        defaultBackground = new Background(new BackgroundFill(lg1, null, null));
    }

    /**
     * Evaluates a the expression belonging to a single anchor.
     * @param anchor The Anchor to return.
     * @return The result of the evaluated anchor clamped between 0 and 1, multiplied by 255.
     *         Or 0 if an error occurred.
     */
    private int evaluateAnchor(InputAnchor anchor) {
        try {
            GhciSession ghci = getPane().getGhciSession().get();
            String result = ghci.pull(anchor.getExpr());

            double v = Math.max(0.0, Math.min(1.0, Double.valueOf(result)));
            return (int) Math.round(v * 255);
        } catch (NumberFormatException | HaskellException | NoSuchElementException e) {
            return 0;
        }
    }

    @Override
    public final void invalidateConnectionState() {
        super.invalidateConnectionState();

        if(r.isPrimaryConnected() || g.isPrimaryConnected() || b.isPrimaryConnected()) {
            int rv = evaluateAnchor(r);
            int gv = evaluateAnchor(g);
            int bv = evaluateAnchor(b);
    
            well.setBackground(new Background(new BackgroundFill(Color.rgb(rv, gv, bv), null, null)));
        } else {
            well.setBackground(defaultBackground);
        }
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(r,g,b);
    }

    @Override
    public List<InputAnchor> getActiveInputs() {
        return getAllInputs();
    }
}
