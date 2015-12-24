package nl.utwente.viskell.ui.components;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.ToplevelPane;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A CLaSH-specific block that is a counterpart to the `simulate` function in Clash. Expects a function that turns a
 * Signal of monotonically increasing numbers into a Signal of representable result values.
 *
 * While signals are infinite, SimulateBlock only shows the first N iterations/steps.
 */
public class SimulateBlock extends Block implements ComponentLoader {
    /** The Anchor that is used as input. */
    protected InputAnchor inputAnchor;

    /** The space containing the input anchor. */
    @FXML protected Pane inputSpace;

    /** The label on which to display type information. */
    @FXML protected Label inputType;

    /** The label on which to display the value of this block */
    @FXML protected Label value;

    @FXML protected Button iterationLabel;

    /** Constrained type variable for the input anchor */
    private final Type funConstraint;

    /** The number of results to calculate and show */
    private int iteration;

    public SimulateBlock(ToplevelPane pane) {
        super(pane);
        loadFXML("SimulateBlock");

        inputAnchor = new InputAnchor(this);
        inputSpace.getChildren().add(inputAnchor);

        iteration = 0;

        String signature = "(Num a, Show b) => Signal a -> Signal b";
        funConstraint = getToplevel().getEnvInstance().buildType(signature);
    }

    @Override
    public final void invalidateVisualState() {
    	this.inputAnchor.invalidateVisualState();
        inputType.setText(inputAnchor.getStringType());

        if (inputAnchor.hasValidConnection()) {
            GhciSession ghciSession = getToplevel().getGhciSession();
            String format = "Data.List.take %d $ simulate (%s) [1..]";
            String expr = String.format(format, iteration, inputAnchor.getFullExpr().toHaskell());
            ListenableFuture<String> result = ghciSession.pullRaw(expr);

            // See DisplayBlock.invalidateVisualState
            Futures.addCallback(result, new FutureCallback<String>() {
                public void onSuccess(String s)    { Platform.runLater(() -> value.setText(s)); }
                public void onFailure(Throwable t) { Platform.runLater(() -> value.setText("?!?!?!")); }
            });
        } else {
            value.setText("?");
        }
    }

    /** Step to the next iteration. */
    public void step() {
        setIteration(iteration + 1);
    }

    /** Reset to the first iteration. */
    public void reset() {
        setIteration(0);
    }

    private void setIteration(int i) {
        iteration = i;
        iterationLabel.setText(String.valueOf(iteration));
        this.invalidateVisualState();
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(inputAnchor);
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of();
    }

    @Override
    public Optional<Block> getNewCopy() {
        return Optional.empty();
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        return inputAnchor.getLocalExpr(outsideAnchors);
    }

    @Override
    public void refreshAnchorTypes() {
        inputAnchor.setFreshRequiredType(funConstraint, new TypeScope());
    }
}
