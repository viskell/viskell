package nl.utwente.viskell.ui.components;

import java.util.*;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.ui.ComponentLoader;

public class Lane extends Pane implements BlockContainer, ComponentLoader {
    
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
    
    boolean updateInProgress;
    
    /** The wrapper to which this alternative belongs */
    protected ChoiceBlock parent;
    
    @FXML protected Pane argumentSpace;
    
    @FXML protected Pane resultSpace;
    
    @FXML protected SplitPane divider;

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
        
        //TODO make the choiceblock draggable from within a lane
        divider.getItems().forEach(element -> element.setMouseTransparent(true));
    }

    public Case.Alternative getAlternative() {
        //TODO generate the pattern and guard for this lane
        return new Case.Alternative(new ConstructorBinder("()", Collections.EMPTY_LIST), new LetExpression(result.getLocalExpr(), true));
    }

    public ResultAnchor getOutput() {
        return result;
    }

    /*@Override
    public void refreshAnchorTypes() {
        // refresh anchor types only once at the start
        if (!firstPhaseInProgress && !freshAnchorTypes) {
            freshAnchorTypes = true;
            
            TypeScope scope = new TypeScope();
            arguments.forEach(argument -> argument.refreshType(scope));
            result.refreshAnchorType(scope);
        }
    }
    
    /*@Override
    public final void handleConnectionChanges(boolean finalPhase) {
        // avoid doing extra work and infinite recursion
        if ((!finalPhase && !firstPhaseInProgress) || (finalPhase && !finalPhaseInProgress)) {
            if (!finalPhase) {
                // in first phase ensure that anchor types are refreshed if propagating from the outside
                refreshAnchorTypes();

                firstPhaseInProgress = true;
                finalPhaseInProgress = false;
            }
            else {
                firstPhaseInProgress = false;
                finalPhaseInProgress = true;
            }

            freshAnchorTypes = false;
            
            // first propagate up from the result anchor
            result.getConnection().ifPresent(c -> c.handleConnectionChangesUpwards(finalPhase));
            
            // also propagate in from above in case the lambda is partially connected 
            arguments.forEach(argument -> {
                argument.getOppositeAnchors().forEach(anchor -> {
                    anchor.handleConnectionChanges(finalPhase);
                    // take the type of argument connections in account even if the connected block is being processed
                    anchor.getConnection().ifPresent(connection -> connection.handleConnectionChangesUpwards(finalPhase));
                });
            });
    
            // propagate internal type changes outwards
            parent.handleConnectionChanges(finalPhase);
        }
    }*/
    @Override
    public void refreshAnchorTypes() {
        if (this.updateInProgress || this.freshAnchorTypes) {
            return; // refresh anchor types only once
        }
        this.freshAnchorTypes = true;
        
        TypeScope scope = new TypeScope();
        for (BinderAnchor arg : arguments) {
            arg.refreshType(scope);
        }
        result.refreshAnchorType(scope);
    }
    
    @Override
    public final void handleConnectionChanges(boolean finalPhase) {
        if (this.updateInProgress != finalPhase) {
            return; // avoid doing extra work and infinite recursion
        }
        
        if (! finalPhase) {
            // in first phase ensure that anchor types are refreshed
            this.refreshAnchorTypes();
        }
        
        this.updateInProgress = !finalPhase;
        this.freshAnchorTypes = false;
        
        // first propagate up from the result anchor
        result.getConnection().ifPresent(c -> c.handleConnectionChangesUpwards(finalPhase));

        // also propagate in from above in case the lambda is partially connected 
        for (BinderAnchor arg : arguments) {
            for (InputAnchor anchor : arg.getOppositeAnchors()) {
                anchor.handleConnectionChanges(finalPhase);
                // take the type of argument connections in account even if the connected block is being processed
                anchor.getConnection().ifPresent(c -> c.handleConnectionChangesUpwards(finalPhase));
            }
        }

        // propagate internal type changes outwards
        parent.handleConnectionChanges(finalPhase);
    }
    

    /** Called when the VisualState changed. */
    public void invalidateVisualState() {
        // TODO update anchors when they get a type label       
    }
    
    @Override
    public void attachBlock(Block block) {
        attachedBlocks.add(block);
    }

    @Override
    public boolean detachBlock(Block block) {
        return attachedBlocks.remove(block);
    }
}
