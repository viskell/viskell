package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Case;
import nl.utwente.viskell.haskell.expr.Case.Alternative;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.expr.LocalVar;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.HaskellTypeError;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeChecker;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.haskell.type.TypeVar;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ToplevelPane;

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
    
    /** The container Node for the Lanes */
    @FXML protected Pane altSpace;
    
    /** The label with the result type of this choiceblock. */
    @FXML private Label signature;
    
    /** The container Node for the OutputAnchor */
    @FXML protected Pane resultSpace;
    
    /** The input space of for this choiceblock */
    @FXML protected Pane inputSpace;
    
    /** The list of input anchors of this choiceblock */
    protected List<ChoiceInputAnchor> inputAnchors;

    public ChoiceBlock(ToplevelPane pane) {
        super(pane);
        this.loadFXML("ChoiceBlock");
        
        lanes = new ArrayList<>();
        output = new OutputAnchor(this, new Binder("choiceoutput"));
        inputAnchors = new ArrayList<>();
        resultSpace.getChildren().add(output);
        dragContext.setGoToForegroundOnContact(false);
        setMinHeight(USE_PREF_SIZE);
        
        addLane();
        addLane();
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return inputAnchors.stream().map(i -> i.anchor).collect(Collectors.toList());
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
        List<TypeVar> typeList = new ArrayList<>();
        TypeVar resultType = TypeScope.unique("choice_res");
        for (int i = 0; i < inputAnchors.size(); ++i) {
            typeList.add(TypeScope.unique("choice_arg"+i));
        }
        
        for (Lane lane : lanes) {
            for (int i = 0; i < typeList.size(); ++i) {
                try {
                    TypeChecker.unify("choice block", typeList.get(i), lane.arguments.get(i).getType(Optional.empty()));
                }
                catch (HaskellTypeError e) {
                }
            }
            try {
                TypeChecker.unify("choice block", resultType, lane.getOutput().getType());
            } catch (HaskellTypeError e) {
            }
        }

        for (int i = 0; i < typeList.size(); ++i) {
            inputAnchors.get(i).anchor.setExactRequiredType(typeList.get(i));
        }
        output.setExactRequiredType(resultType);
    }

    public void handleConnectionChanges(boolean finalPhase) {
        lanes.forEach(lane -> lane.handleConnectionChanges(finalPhase));

        // continue as normal with propagating changes on the outside
        super.handleConnectionChanges(finalPhase);
    }
    
    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        List<Alternative> bindings = lanes.stream().map(lane -> lane.getAlternative(outsideAnchors)).collect(Collectors.toList());
        LetExpression let = new LetExpression(new Case(new Value(Type.tupleOf(), "()"), bindings), false);
        lanes.forEach(lane -> {
            for (int i = 0; i < getAllInputs().size(); ++i) {
                int j = i;
                getAllInputs().get(i).getOppositeAnchor().ifPresent(anchor -> let.addLetBinding(lane.arguments.get(j).binder, new LocalVar(anchor.binder)));
            }
        });
        
        getAllInputs().forEach(in -> in.getOppositeAnchor().ifPresent(out -> outsideAnchors.add(out)));
        return let;
    }

    @Override
    protected void extendExprGraph(LetExpression exprGraph, BlockContainer container, Set<OutputAnchor> outsideAnchors) {
        return; // FIXME this override is only because Choice block return internal anchors in getAllInputs()
   }

    @Override
    public void invalidateVisualState() {
        lanes.forEach(lane -> lane.invalidateVisualState());
        inputAnchors.forEach(ChoiceInputAnchor::invalidateVisualState);
        signature.setText(output.getStringType());
        output.invalidateVisualState();
    }
    
    @Override
    public boolean belongsOnBottom() {
        return true;
    }

    /** Adds an alternative to this block */
    public void addLane() {
        Lane lane = new Lane(this);
        lanes.add(lane);
        getAllInputs().forEach(anchor -> lane.addExtraInput());
        altSpace.getChildren().add(lane);
        initiateConnectionChanges();
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
    
    /** Adds extra input anchor to this block */
    public void addExtraInput() {
        ChoiceInputAnchor arg = new ChoiceInputAnchor();
        inputAnchors.add(arg);
        inputSpace.getChildren().add(arg);
        lanes.forEach(Lane::addExtraInput);
        
        inputSpace.setMinHeight(35);
        initiateConnectionChanges();
    }

    /** Removes the last input anchor of this block */
    public void removeLastInput() {
        if (!inputAnchors.isEmpty()) {
            ChoiceInputAnchor arg = inputAnchors.remove(inputAnchors.size()-1);
            arg.anchor.removeConnections();
            inputSpace.getChildren().remove(arg);
            lanes.forEach(Lane::removeLastInput);
            
            initiateConnectionChanges();
        }
        
        if (inputAnchors.isEmpty()) {
            inputSpace.setMinHeight(BASELINE_OFFSET_SAME_AS_HEIGHT);
        }
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

    /** Combined input anchor and type label. */
    private class ChoiceInputAnchor extends VBox implements ConnectionAnchor.Target {

        /** The connection anchor for this input argument. */
        private final InputAnchor anchor;
        
        /** The input type label of this anchor. */
        private final Label inputType;

        private ChoiceInputAnchor() {
            this.inputType = new Label(".....");
            this.inputType.setMinWidth(USE_PREF_SIZE);
            this.inputType.getStyleClass().add("inputType");
            this.inputType.setAlignment(Pos.CENTER);
            this.anchor = new InputAnchor(ChoiceBlock.this);
            this.anchor.setAlignment(Pos.CENTER);
            this.getChildren().addAll(this.anchor, this.inputType);
            this.setTranslateY(-9);
            this.setPickOnBounds(false);
            this.setAlignment(Pos.CENTER);
        }

        @Override
        public ConnectionAnchor getAssociatedAnchor() {
            return this.anchor;
        }

        /** Refresh visual information such as types */
        private void invalidateVisualState() {
            this.anchor.invalidateVisualState();
            boolean validConnection = this.anchor.hasValidConnection();
            this.setTranslateY(validConnection ? 0 : -9);
            this.inputType.setText(validConnection ? "zyxwv" : this.anchor.getStringType()); 
            this.inputType.setVisible(!validConnection);
        }
    }
    
}
