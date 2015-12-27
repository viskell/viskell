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
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
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
        
    	this.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> event.consume());
    	this.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> event.consume());
    	this.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
    			if (event.getButton() != MouseButton.PRIMARY) {
    				Point2D menuPos = this.parent.getToplevel().screenToLocal(new Point2D(event.getScreenX(), event.getScreenY()));
    				this.parent.getToplevel().showFunctionMenuAt(menuPos.getX(), menuPos.getY(), true);
    			}
    		   	event.consume();
    		});
    	this.addEventHandler(TouchEvent.TOUCH_PRESSED, event -> event.consume());
    	this.addEventHandler(TouchEvent.TOUCH_MOVED, event -> event.consume());
    	this.addEventHandler(TouchEvent.TOUCH_RELEASED, event -> {
    			if (event.getTouchPoints().stream().filter(tp -> tp.belongsTo(this)).count() == 2) {
    				Point2D screenPos = new Point2D(event.getTouchPoint().getScreenX(), event.getTouchPoint().getScreenY());
    				Point2D menuPos = this.parent.getToplevel().screenToLocal(screenPos);
    				this.parent.getToplevel().showFunctionMenuAt(menuPos.getX(), menuPos.getY(), false);
    			}
    			event.consume();
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
            } else {
                block.getAllOutputs().forEach(anchor -> {
                    guards.addLetBinding(new ConstructorBinder("True"), anchor.getVariable());
                    anchor.extendExprGraph(guards, this, outsideAnchors);
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
        this.resizer.relocate(300-20, 400-20);

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
    public Bounds getBoundsInScene() {
        return this.localToScene(this.getBoundsInLocal());
    }
    
    @Override
    public void deleteAllLinks() {
        this.arguments.forEach(OutputAnchor::removeConnections);
        this.result.removeConnections();
        new ArrayList<>(this.attachedBlocks).forEach(block -> block.moveIntoContainer(this.getParentContainer()));
    }
}
