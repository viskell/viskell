package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nl.utwente.viskell.haskell.expr.Case;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeChecker;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.haskell.type.TypeVar;
import nl.utwente.viskell.ui.CustomUIPane;

import com.google.common.collect.ImmutableList;

/**
 * An evaluation block with multiple guarded alternatives.
 *
 */
public class ChoiceBlock extends Block {
    
    /** The alternatives inside this block */
    protected List<Lane> lanes;
    
    /** The output anchor of this block */
    protected OutputAnchor output;

    public ChoiceBlock(CustomUIPane pane) {
        super(pane);
        this.loadFXML("ChoiceBlock");
        
        lanes = new ArrayList<>();
        addLane();
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of();
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of(output);
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
        lanes.stream().forEach(lane -> handleConnectionChanges(finalPhase));

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

    
    public void addLane() {
        lanes.add(new Lane(this));
    }
    
    public void removeLane(int index) {
        lanes.remove(index);
        handleConnectionChanges(false);
        handleConnectionChanges(true);
    }
}
