package nl.utwente.group10.ui.components.blocks.input;

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
import nl.utwente.group10.ghcj.GhciException;
import nl.utwente.group10.ghcj.GhciSession;
import nl.utwente.group10.haskell.hindley.HindleyMilner;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.InputAnchor;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Block with three inputs that represent RGB values.
 * The RGBBlock colors according to these inputs.
 */
public class RGBBlock extends DisplayBlock {
    /** The InputAnchor to which the Red value should be connected. **/
    private InputAnchor r;
    
    /** The InputAnchor to which the Green value should be connected. **/
    private InputAnchor g;
    
    /** The InputAnchor to which the Blue value should be connected. **/
    private InputAnchor b;

    /** The Pane to paint with the currently inputed color. **/
    @FXML private Pane well;
    
    /** Default Background to use when no values are inputed. **/
    private Background defaultBackground;

    public RGBBlock(CustomUIPane pane) {
        super(pane, "RGBBlock");

        r = super.getAllInputs().get(0);
        g = new InputAnchor(this); //, HindleyMilner.makeVariable());
        b = new InputAnchor(this); //, HindleyMilner.makeVariable());
        inputSpace.getChildren().setAll(ImmutableList.of(r, g, b));
        
        // Spread them out over the top of the Block.
        r.layoutXProperty().bind(inputSpace.widthProperty().divide(3 / 0.5));
        g.layoutXProperty().bind(inputSpace.widthProperty().divide(3 / 1.5));
        b.layoutXProperty().bind(inputSpace.widthProperty().divide(3 / 2.5));
        
        //Make sure inputSpace is drawn on top.
        BorderPane borderPane = (BorderPane) inputSpace.getParent();
        borderPane.getChildren().remove(inputSpace);
        borderPane.setTop(inputSpace);

        Stop[] stops = new Stop[] { new Stop(0, Color.RED), new Stop(0.5, Color.GREEN), new Stop(1, Color.BLUE)};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        defaultBackground = new Background(new BackgroundFill(lg1, null, null));
        well.setBackground(defaultBackground);
    }

    private int evaluate(InputAnchor anchor) {
        try {
            GhciSession ghci = getPane().getGhciSession().get();
            String result = ghci.pull(anchor.getExpr());

            double v = Math.max(0.0, Math.min(1.0, Double.valueOf(result)));
            return (int) Math.round(v * 255);
        } catch (NumberFormatException | GhciException | NoSuchElementException e) {
            return 0;
        }
    }

    @Override
    public final void invalidateConnectionState() {
        super.invalidateConnectionState();

        if(r.isPrimaryConnectedCorrect() || g.isPrimaryConnectedCorrect() || b.isPrimaryConnectedCorrect()) {
            int rv = evaluate(r);
            int gv = evaluate(g);
            int bv = evaluate(b);
    
            well.setBackground(new Background(new BackgroundFill(Color.rgb(rv, gv, bv), null, null)));
        } else {
            well.setBackground(defaultBackground);
        }
    }

    /**
     * @return The InputAnchor as specified by the given index.
     */
    private InputAnchor getInput(int index) {
        return getAllInputs().get(index);
    }
    
    /*
    @Override
    public Type getInputSignature(int index) {
        return getInput(index).getSignature();
    }
    */

    @Override
    public Type getInputType(int index) {
        throw new RuntimeException();
        //return getInput(index).getType();
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return Arrays.asList(r,g,b);
    }

    @Override
    public List<InputAnchor> getActiveInputs() {
        return getAllInputs();
    }
}
