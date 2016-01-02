package nl.utwente.viskell.ui.components;

import java.util.Optional;
import java.util.Set;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;

public class LocalDefUse extends Label implements FunctionReference {

    private final LambdaBlock definition;
    
    public LocalDefUse(LambdaBlock definition) {
        super(definition.getName());
        this.definition = definition;
        this.setMinWidth(USE_PREF_SIZE);
        this.setMaxWidth(USE_PREF_SIZE);
        this.getStyleClass().add("title");
    }

    @Override
    public void initializeBlock(Block funBlock) {
        // TODO
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
    

}
