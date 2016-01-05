package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.viskell.haskell.expr.Case;
import nl.utwente.viskell.haskell.expr.ConstructorBinder;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.ComponentLoader;
import nl.utwente.viskell.ui.DragContext;
import nl.utwente.viskell.ui.TouchContext;

/**
 * A single alternative within a ChoiceBlock
 *
 */
public class Lane extends BorderPane implements WrappedContainer, ComponentLoader {
    
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
    private Pane resizer;

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
        
        TouchContext context = new TouchContext(this);
        context.setPanningAction((deltaX, deltaY) -> {
            if (! this.parent.isActivated()) {
                this.parent.relocate(this.parent.getLayoutX() + deltaX, this.parent.getLayoutY() + deltaY);
            }
        });
    }

    /**
     * Returns the pattern and guard expression for this lane as well as a set of required blocks.
     * 
     * Firstly, the expression is generated from the result anchor.
     * Secondly, the expression is extended with bottom-most blocks within this lane.
     * Bottom-most blocks are *assumed* to be either deconstructors or expressions resulting in a Bool.
     * 
     * @return this lane's Alternative
     */
    public Case.Alternative getAlternative(Set<OutputAnchor> outsideAnchors) {
        LetExpression guards = new LetExpression(result.getLocalExpr(outsideAnchors), true);
        result.extendExprGraph(guards, this, outsideAnchors);
        
        attachedBlocks.stream().filter(Block::isBottomMost).forEach(block -> {
            if (block instanceof MatchBlock) {
                guards.addLetBinding(((MatchBlock)block).getPrimaryBinder(), block.getAllInputs().get(0).getFullExpr());
            } else if (block instanceof SplitterBlock) {
                guards.addLetBinding(((SplitterBlock)block).getPrimaryBinder(), block.getAllInputs().get(0).getFullExpr());
            } else {
                block.getAllOutputs().forEach(anchor -> {
                    if (anchor.getStringType().equals("Bool")) {
                        guards.addLetBinding(new ConstructorBinder("True"), anchor.getVariable());
                        anchor.extendExprGraph(guards, this, outsideAnchors);
                    }
                });
            }
            block.extendExprGraph(guards, this, outsideAnchors);
        });
        
        return (new Case.Alternative(new ConstructorBinder("()"), guards));
    }

    /** Returns the result anchor of this Lane */
    public ResultAnchor getOutput() {
        return result;
    }

    public List<ConnectionAnchor> getAllAnchors() {
        List<ConnectionAnchor> anchors = new ArrayList<>(this.arguments);
        anchors.add(this.result);
        return anchors;
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
    	this.result.invalidateVisualState();
    }
    
    /** Add and initializes a resizer element to this block */
    private void setupResizer() {
        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(new Double[]{20.0, 20.0, 20.0, 0.0, 0.0, 20.0});
        triangle.setFill(Color.BLUE);

        this.resizer = new Pane(triangle);
        triangle.setLayoutX(10);
        triangle.setLayoutY(10);
        this.resizer.setManaged(false);
        this.getChildren().add(this.resizer);
        this.resizer.relocate(240-20, 320-20);

        DragContext sizeDrag = new DragContext(this.resizer);
        sizeDrag.setDragLimits(new BoundingBox(200, 200, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    
    @Override
    public ChoiceBlock getWrapper() {
        return this.parent;
    }
    
    @Override
    public void attachBlock(Block block) {
        attachedBlocks.add(block);
    }

    @Override
    public void detachBlock(Block block) {
        attachedBlocks.remove(block);
    }
    
    @Override
    public Stream<Block> getAttachedBlocks() {
        return this.attachedBlocks.stream();
    }

    @Override
    public BlockContainer getParentContainer() {
        return parent.getContainer();
    }

    @Override
    public Node asNode() {
        return this;
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
    
    @Override
    public Bounds containmentBoundsInScene() {
        Bounds local = this.getBoundsInLocal();
        // include border area around this lane
        BoundingBox withBorders = new BoundingBox(local.getMinX()-10, local.getMinY()-25, local.getWidth()+20, local.getHeight()+50);
        return this.localToScene(withBorders);
    }
    
    @Override
    public void deleteAllLinks() {
        this.arguments.forEach(OutputAnchor::removeConnections);
        this.result.removeConnections();
        new ArrayList<>(this.attachedBlocks).forEach(block -> block.moveIntoContainer(this.getParentContainer()));
    }

    @Override
    public void expandToFit(Bounds blockBounds) {
        Bounds containerBounds = this.parent.getToplevel().sceneToLocal(this.localToScene(this.getBoundsInLocal()));
        double shiftX = Math.min(0, blockBounds.getMinX() - containerBounds.getMinX());
        double shiftY = Math.min(0, blockBounds.getMinY() - containerBounds.getMinY());
        double extraX = Math.max(0, blockBounds.getMaxX() - containerBounds.getMaxX()) + Math.abs(shiftX);
        double extraY = Math.max(0, blockBounds.getMaxY() - containerBounds.getMaxY()) + Math.abs(shiftY);
        this.resizer.relocate(this.resizer.getLayoutX() + extraX, this.resizer.getLayoutY() + extraY);
        double shiftXForRights = extraX + shiftX;
        this.parent.shiftAllBut(shiftX, shiftY, this, shiftXForRights);

        // also resize its parent in case of nested containers
        Bounds fitBounds = this.parent.getBoundsInParent();
        this.getParentContainer().expandToFit(new BoundingBox(fitBounds.getMinX()-10, fitBounds.getMinY()-10, fitBounds.getWidth()+20, fitBounds.getHeight()+20));
    }
}
