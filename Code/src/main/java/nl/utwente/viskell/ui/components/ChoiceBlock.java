package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Case;
import nl.utwente.viskell.haskell.expr.Case.Alternative;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeChecker;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.haskell.type.TypeVar;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ToplevelPane;

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
    
    /** The label with the result type of this choiceblock. */
    @FXML private Label signature;
    
    /** The container Node for the OutputAnchor */
    @FXML protected Pane resultSpace;

    public ChoiceBlock(ToplevelPane pane) {
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
    public List<ConnectionAnchor> getAllAnchors() {
        List<ConnectionAnchor> result = new ArrayList<>();
        result.add(this.output);
        for (Lane lane : this.lanes) {
            result.addAll(lane.getAllAnchors());
        }
        return result;
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
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        List<Alternative> bindings = lanes.stream().map(lane -> lane.getAlternative(outsideAnchors)).collect(Collectors.toList());
        return new Case(new Value(Type.tupleOf(), "()"), bindings);
    }

    @Override
    protected void extendExprGraph(LetExpression exprGraph, BlockContainer container, Set<OutputAnchor> outsideAnchors) {
        return; // FIXME this override is only because Choice block return internal anchors in getAllInputs()
   }

    @Override
    public void invalidateVisualState() {
        lanes.forEach(lane -> lane.invalidateVisualState());
        signature.setText(output.getStringType());
        this.output.invalidateVisualState();
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
        this.initiateConnectionChanges();
    }
    
    /** Removes the last alternative from this block */
    public void removeLastLane() {
        if (this.lanes.size() > 1) { 
            Lane lane = lanes.remove(lanes.size()-1);
            lane.deleteAllLinks();
            altSpace.getChildren().remove(lane);
            this.initiateConnectionChanges();
        }
    }
    
    /** Returns the alternatives in this block */
    public List<Lane> getLanes() {
        return lanes;
    }
    
    @Override
    public List<Lane> getInternalContainers() {
        return ImmutableList.copyOf(this.lanes);
    }
    
    @Override
    public void relocate(double x, double y) {
        double dx = x-getLayoutX(), dy = y-getLayoutY();
        super.relocate(x, y);
        
        lanes.forEach(lane -> lane.moveNodes(dx, dy));
    }

    protected void shiftAllBut(double shiftX, double shiftY, Lane changedLane, double shiftXForRights) {
        super.relocate(this.getLayoutX() + shiftX, this.getLayoutY() + shiftY);
        int lx = this.lanes.indexOf(changedLane);
        for (int i = 0; i < this.lanes.size(); i++) {
            if (i < lx) {
                this.lanes.get(i).moveNodes(shiftX, 0);
            } else if (i > lx) {
                this.lanes.get(i).moveNodes(shiftXForRights, 0);
            }
        }
    }
    
    @Override
    public void deleteAllLinks() {
        lanes.forEach(lane -> lane.deleteAllLinks());
        super.deleteAllLinks();
    }

}
