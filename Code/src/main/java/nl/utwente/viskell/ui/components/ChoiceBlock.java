package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Case;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeChecker;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.haskell.type.TypeVar;
import nl.utwente.viskell.ui.CustomUIPane;

/**
 * An evaluation block with multiple guarded alternatives.
 *
 */
public class ChoiceBlock extends Block {
    
    /** The alternatives inside this block */
    protected List<Lane> lanes;
    
    /** The output anchor of this block */
    protected OutputAnchor output;
    
    @FXML protected Pane altSpace;
    
    @FXML protected Pane funSpace;

    public ChoiceBlock(CustomUIPane pane) {
        super(pane);
        this.loadFXML("ChoiceBlock");
        
        lanes = new ArrayList<>();
        output = new OutputAnchor(this, new Binder("choiceoutput"));
        funSpace.getChildren().add(output);
        addLane();
        addLane();
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return lanes.stream().map(Lane::getOutput).collect(Collectors.toList());
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return Collections.singletonList(output);
    }

    @Override
    protected void refreshAnchorTypes() {
        lanes.stream().forEach(lane -> lane.handleConnectionChanges(false));
        
        // TODO make sure the last edited lane gets unified last
        TypeVar type = TypeScope.unique("lanetype");
        for (Lane lane : lanes) {
            try {
                TypeChecker.unify("choice block", type, lane.getOutput().getType());
            } catch (HaskellTypeError e) {
                // TODO mark result anchors as invalid
            }
        }

        output.setExactRequiredType(type);
    }

    public void handleConnectionChanges(boolean finalPhase) {
        lanes.stream().forEach(lane -> lane.handleConnectionChanges(finalPhase));

        // continue as normal with propagating changes on the outside
        super.handleConnectionChanges(finalPhase);
    }
    
    @Override
    public Expression getLocalExpr() {
        return new Case(new Value(Type.tupleOf(), "()"), lanes.stream().map(Lane::getAlternative).collect(Collectors.toList()));
    }

    @Override
    public void invalidateVisualState() {
        //TODO fill in
        lanes.forEach(lane -> lane.invalidateVisualState());
    }
    
    @Override
    public boolean belongsOnBottom() {
        return true;
    }

    
    public void addLane() {
        Lane lane = new Lane(this);
        lanes.add(lane);
        altSpace.getChildren().add(lane);
    }
    
    public void removeLane(int index) {
        lanes.remove(index);
        altSpace.getChildren().remove(index);
        handleConnectionChanges(false);
        handleConnectionChanges(true);
    }

    public List<Lane> getLanes() {
        return lanes;
    }
}
