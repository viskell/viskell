package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ui.CustomUIPane;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

/**
 * Block with three inputs that represent RGB values.
 * The RGBBlock colors according to these inputs.
 */
public class RGBBlock extends DisplayBlock {
    /** The InputAnchors to which the red, green and blue values should be connected. **/
    private final InputAnchor r, g, b;
    
    /** The Pane to paint with the currently inputed color. **/
    @FXML private Pane well;
    
    /** Default Background to use when no values are inputed. **/
    private final Background defaultBackground;

    public RGBBlock(CustomUIPane pane) {
        super(pane, "RGBBlock");

        r = super.getAllInputs().get(0);
        g = new InputAnchor(this);
        b = new InputAnchor(this);

        inputSpace.getChildren().setAll(r, g, b);
        
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
            GhciSession ghci = getPane().getGhciSession();
            String result = ghci.pull(anchor.getFullExpr()).get();

            double v = Math.max(0.0, Math.min(1.0, Double.valueOf(result)));
            return (int) Math.round(v * 255);
        } catch (NumberFormatException | NoSuchElementException | InterruptedException | ExecutionException e) {
            return 0;
        }
    }

    @Override
    public final void invalidateVisualState() {
    	r.invalidateVisualState();
    	g.invalidateVisualState();
    	b.invalidateVisualState();
    	
        if(r.hasConnection() || g.hasConnection() || b.hasConnection()) {
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
        return ImmutableList.of(r, g, b);
    }
}
