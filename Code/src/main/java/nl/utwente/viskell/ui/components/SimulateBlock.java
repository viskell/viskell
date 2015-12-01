package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.CustomUIPane;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SimulateBlock extends Block implements ComponentLoader {
    /** The Anchor that is used as input. */
    protected InputAnchor inputAnchor;

    /** The space containing the input anchor. */
    @FXML
    protected Pane inputSpace;

    /** The label on which to display type information. */
    @FXML protected Label inputType;

    /** The label on which to display the value of this block */
    @FXML protected Label value;

    @FXML private SimpleIntegerProperty iteration;

    @FXML protected Label iterationLabel;

    public SimulateBlock(CustomUIPane pane) {
        super(pane);
        loadFXML("SimulateBlock");

        inputAnchor = new InputAnchor(this);
        inputSpace.getChildren().add(inputAnchor);

        iteration = new SimpleIntegerProperty(0);
        iteration.addListener(e -> this.invalidateVisualState());
        iteration.addListener(e -> this.iterationLabel.setText(String.valueOf(iteration.get())));
    }

    @Override
    public final void invalidateVisualState() {
        this.inputType.setText(this.inputAnchor.getStringType());

        if (inputAnchor.hasConnection()) {
            try {
                GhciSession ghciSession = getPane().getGhciSession();
                String funName = "sim_fun_" + Integer.toHexString(this.hashCode());
                ghciSession.push(funName, inputAnchor.getFullExpr());
                String results = ghciSession.pullRaw("Data.List.take " + iteration.get() + " $ simulate " + funName + " [1..]").get();
                value.setText(results);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            value.setText("?");
        }
    }

    @FXML private void step() {
        iteration.set(iteration.get() + 1);
    }

    @FXML private void reset() {
        iteration.set(0);
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
    public Expression getLocalExpr() {
        return inputAnchor.getLocalExpr();
    }

    @Override
    public void refreshAnchorTypes() {
        this.inputAnchor.setFreshRequiredType(getPane().getEnvInstance().buildType("Signal a -> Signal b"), new TypeScope());
    }
}
