package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.viskell.haskell.expr.Case;
import nl.utwente.viskell.haskell.expr.ConstructorBinder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.DragContext;

/**
 * A single alternative within a ChoiceBlock
 *
 */
public class Lane extends BorderPane implements BlockContainer, ComponentLoader {
    
    /** The argument anchors of this alternative */
    protected List<BinderAnchor> arguments;
    
    /** The result anchor of this alternative */
    protected ResultAnchor result;

    /** Whether the anchor types are fresh*/
    protected boolean freshAnchorTypes;
    
    /** Status of change updating process in this block. */
    protected boolean firstPhaseInProgress;
    
    /** Status of change updating process in this block. */
    protected boolean finalPhaseInProgress;
    
    /** The wrapper to which this alternative belongs */
    protected ChoiceBlock parent;
    
    /** The draggable resizer in the bottom right corner */
    private Polygon resizer;

    /** The container Node for binder anchors */
    @FXML protected Pane argumentSpace;
    
    /** The resizable area on which blocks can be placed */
    @FXML protected Pane guardSpace;
    
    /** The container Node for result anchors */
    @FXML protected Pane resultSpace;

    /** A set of blocks that belong to this container */
    protected Set<Block> attachedBlocks;

    public Lane(ChoiceBlock wrapper) {
        super();
        loadFXML("Lane");
        parent = wrapper;
        arguments = new ArrayList<>();
        result = new ResultAnchor(this, wrapper, Optional.empty());
        attachedBlocks = new HashSet<>();
        
        argumentSpace.getChildren().addAll(arguments);
        resultSpace.getChildren().add(result);
        setupResizer();
    }

    /**
     * Returns the pattern and guard expression for this lane as well as a set of required blocks.
     * 
     * Firstly, the expression is generated from the result anchor.
     * Secondly, the expression is extended with bottom-most blocks within this lane.
     * Bottom-most blocks are *assumed* to be either deconstructors or expressions resulting in a Bool.
     * 
     * @return a pair containing this lane's Alternative and a set of out-of-reach OutputAnchors
     */
    public Pair<Case.Alternative,Set<OutputAnchor>> getAlternative() {
        Pair<Expression, Set<OutputAnchor>> pair = result.getLocalExpr();
        LetExpression guards = new LetExpression(pair.a, true);
        Set<OutputAnchor> outsideAnchors = pair.b;
        result.extendExprGraph(guards, Optional.of(this), outsideAnchors);
        
        attachedBlocks.stream().filter(Block::isBottomMost).forEach(block -> {
            if (block instanceof MatchBlock) {
                guards.addLetBinding(((MatchBlock)block).getPrimaryBinder(), block.getAllInputs().get(0).getFullExpr());
            } else {
                block.getAllOutputs().forEach(anchor -> {
                    guards.addLetBinding(new ConstructorBinder("True"), anchor.getVariable());
                    anchor.extendExprGraph(guards, Optional.of(this), outsideAnchors);
                });
            }
            block.extendExprGraph(guards, Optional.of(this), outsideAnchors);
        });
        
        return new Pair<>(new Case.Alternative(new ConstructorBinder("()"), guards), outsideAnchors);
    }

    /** Returns the result anchor of this Lane */
    public ResultAnchor getOutput() {
        return result;
    }

    @Override
    public void refreshAnchorTypes() {
        // refresh anchor types only once at the start of the typechecking process
        if (!firstPhaseInProgress && !freshAnchorTypes) {
            freshAnchorTypes = true;
            
            TypeScope scope = new TypeScope();
            arguments.forEach(argument -> argument.refreshType(scope));
            result.refreshAnchorType(scope);
        }
    }
    
    @Override
    public final void handleConnectionChanges(boolean finalPhase) {
        // avoid doing extra work and infinite recursion
        if ((!finalPhase && !firstPhaseInProgress) || (finalPhase && !finalPhaseInProgress)) {
            if (!finalPhase) {
                // in first phase ensure that anchor types are refreshed if propagating from the outside
                refreshAnchorTypes();

                firstPhaseInProgress = true;
                finalPhaseInProgress = false;
            } else {
                firstPhaseInProgress = false;
                finalPhaseInProgress = true;
            }

            freshAnchorTypes = false;
            
            // first propagate up from the result anchor
            result.getConnection().ifPresent(c -> c.handleConnectionChangesUpwards(finalPhase));
            
            // also propagate in from above in case the lane is partially connected 
            arguments.forEach(argument -> {
                argument.getOppositeAnchors().forEach(anchor -> {
                    anchor.handleConnectionChanges(finalPhase);
                    // take the type of argument connections into account even if the connected block is being processed
                    anchor.getConnection().ifPresent(connection -> connection.handleConnectionChangesUpwards(finalPhase));
                });
            });
    
            // propagate internal type changes outwards
            parent.handleConnectionChanges(finalPhase);
        }
    }

    /** Called when the VisualState changed. */
    public void invalidateVisualState() {
        // TODO update anchors when they get a type label       
    }
    
    /** Add and initializes a resizer element to this block */
    private void setupResizer() {
        resizer = new Polygon();
        resizer.getPoints().addAll(new Double[]{20.0, 20.0, 20.0, 0.0, 0.0, 20.0});
        resizer.setFill(Color.BLUE);

        resizer.setManaged(false);
        this.getChildren().add(resizer);
        resizer.relocate(300-20, 400-20);

        DragContext sizeDrag = new DragContext(resizer);
        sizeDrag.setDragLimits(new BoundingBox(200, 200, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    
    @Override
    public void attachBlock(Block block) {
        attachedBlocks.add(block);
        handleConnectionChanges(false);
        handleConnectionChanges(true);
        block.handleConnectionChanges(false);
        block.handleConnectionChanges(true);
    }

    @Override
    public boolean detachBlock(Block block) {
        if (attachedBlocks.remove(block)) {
            block.detachFromContainer();
            handleConnectionChanges(false);
            handleConnectionChanges(true);
            block.handleConnectionChanges(false);
            block.handleConnectionChanges(true);
            return true;
        }
        else {
            return false;
        }
    }
    
    @Override
    public void detachAllBlocks() {
        attachedBlocks.forEach(Block::detachFromContainer);
    }
    
    @Override
    public boolean containsBlock(Block block) {
        return attachedBlocks.contains(block);
    }

    @Override
    public Optional<BlockContainer> getContainer() {
        return parent.getContainer();
    }
    
    @Override
    public void moveNodes(double dx, double dy) {
        attachedBlocks.forEach(node -> node.relocate(node.getLayoutX()+dx, node.getLayoutY()+dy));
    }
    
    @Override
    protected double computePrefWidth(double height) {
        guardSpace.setPrefWidth(resizer.getBoundsInParent().getMaxX());
        return super.computePrefWidth(height);
    }
    
    @Override 
    protected double computePrefHeight(double width) {
        double resizerY = resizer.getLayoutY();
        parent.getLanes().stream().filter(lane -> lane.resizer.getLayoutY() != resizerY).forEach(lane -> lane.resizer.setLayoutY(resizerY));
        guardSpace.setPrefHeight(resizer.getBoundsInParent().getMaxY());
        return super.computePrefHeight(width);
    }
}
