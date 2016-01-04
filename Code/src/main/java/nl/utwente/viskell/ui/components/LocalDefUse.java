package nl.utwente.viskell.ui.components;

import java.util.Optional;
import java.util.Set;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.BlockContainer;

public class LocalDefUse extends Label implements FunctionReference {

    private final LambdaBlock definition;
    
    private Block funBlock;
    
    public LocalDefUse(LambdaBlock definition) {
        super(definition.getName());
        this.definition = definition;
        this.setMinWidth(USE_PREF_SIZE);
        this.setMaxWidth(USE_PREF_SIZE);
        this.getStyleClass().add("title");
    }

    @Override
    public void initializeBlock(Block funBlock) {
        this.funBlock = funBlock;
    }

    @Override
    public Optional<InputAnchor> getInputAnchor() {
        return Optional.empty();
    }

    @Override
    public int requiredArguments() {
        return this.definition.getBody().argCount();
    }

    @Override
    public Type refreshedType(int argCount, TypeScope scope) {
        return this.definition.getBinder().getBoundType().getFresh(scope);
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        // gather everything that is needed for the definition, even though we don't use the resulting expression here
        this.definition.getLocalExpr(outsideAnchors);
        
        outsideAnchors.addAll(this.definition.getAllOutputs());
        return new LocalVar(this.definition.getBinder());
    }

    @Override
    public void invalidateVisualState() {
    }
    
    @Override
    public Region asRegion() {
        return this;
    }

    @Override
    public FunctionReference getNewCopy() {
        return new LocalDefUse(this.definition);
    }

    @Override
    public String getName() {
        return this.definition.getName();
    }

    @Override
    public boolean isScopeCorrectIn(BlockContainer container) {
        return container.isContainedWithin(this.definition.getContainer());
    }

    public void handleConnectionChanges(boolean finalPhase) {
        this.funBlock.handleConnectionChanges(finalPhase);        
    }
    
    public void onDefinitionRemoved() {
        ApplyAnchor apply = new ApplyAnchor(this.requiredArguments());
        if (this.funBlock instanceof FunctionBlock) {
            ((FunctionBlock)this.funBlock).convertToOpenApply(apply);
        } else if (this.funBlock instanceof FunApplyBlock) {
            ((FunApplyBlock)this.funBlock).convertToOpenApply(apply);
        }
    }

    @Override
    public void deleteLinks() {
        this.definition.removeUser(this);        
    }

}
