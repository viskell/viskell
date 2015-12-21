package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Case;
import nl.utwente.viskell.haskell.expr.Case.Alternative;
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
    
    /** The container Node for the Lanes */
    @FXML protected Pane altSpace;
    
    /** The container Node for the OutputAnchor */
    @FXML protected Pane resultSpace;

    public ChoiceBlock(CustomUIPane pane) {
        super(pane);
        this.loadFXML("ChoiceBlock");
        
        lanes = new ArrayList<>();
        output = new OutputAnchor(this, new Binder("choiceoutput"));
        resultSpace.getChildren().add(output);
        dragContext.setGoToForegroundOnContact(false);
        
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
    public Optional<Block> getNewCopy() {
        // copying the internal is too complex for now
        return Optional.empty();
    }

    @Override
    protected void refreshAnchorTypes() {
        lanes.stream().forEach(lane -> lane.handleConnectionChanges(false));
        
        // TODO make sure the last edited lane gets unified last to prevent
        // that large parts of a program become invalid in case of a type error,
        // but rather only the lane in which the edit took place.
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
        lanes.forEach(lane -> lane.handleConnectionChanges(finalPhase));

        // continue as normal with propagating changes on the outside
        super.handleConnectionChanges(finalPhase);
    }
    
    @Override
    public Pair<Expression, Set<OutputAnchor>> getLocalExpr() {
        List<Alternative> bindings = lanes.stream().map(Lane::getAlternative).map(pair -> pair.a).collect(Collectors.toList());
        Set<OutputAnchor> outsideAnchors = lanes.stream().map(Lane::getAlternative).flatMap(pair -> pair.b.stream()).collect(Collectors.toSet());
        
        return new Pair<>(new Case(new Value(Type.tupleOf(), "()"), bindings), outsideAnchors);
    }

    @Override
    public void invalidateVisualState() {
        // TODO update anchors when they get a type label       
        lanes.forEach(lane -> lane.invalidateVisualState());
    }
    
    @Override
    public boolean belongsOnBottom() {
        return true;
    }

    /** Adds an alternative to this block */
    public void addLane() {
        Lane lane = new Lane(this);
        lanes.add(lane);
        altSpace.getChildren().add(lane);
    }
    
    /** Returns the alternatives in this block */
    public List<Lane> getLanes() {
        return lanes;
    }
    
    @Override
    public void relocate(double x, double y) {
        double dx = x-getLayoutX(), dy = y-getLayoutY();
        super.relocate(x, y);
        
        lanes.forEach(lane -> lane.moveNodes(dx, dy));
    }
    
    @Override
    public void deleteAllLinks() {
        lanes.forEach(lane -> lane.deleteAllLinks());
        super.deleteAllLinks();
    }
}
