package nl.utwente.viskell.ui.components;

import java.util.Optional;
import java.util.Set;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.BlockContainer;

public class LibraryFunUse extends Label implements FunctionReference {
    
    private final FunctionInfo funInfo;
    
    public LibraryFunUse(FunctionInfo funInfo) {
        super(funInfo.getDisplayName());
        this.funInfo = funInfo;
        this.setMinWidth(USE_PREF_SIZE);
        this.setMaxWidth(USE_PREF_SIZE);
        this.getStyleClass().add("title");
    }

    public FunctionInfo getFunInfo() {
        return this.funInfo;
    }
    
    @Override
    public void initializeBlock(Block funBlock) {
    }

    @Override
    public Optional<InputAnchor> getInputAnchor() {
        return Optional.empty();
    }

    @Override
    public int requiredArguments() {
        return this.funInfo.argumentCount();
    }

    @Override
    public Type refreshedType(int argCount, TypeScope scope) {
        return this.funInfo.getFreshSignature();
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        return new FunVar(this.funInfo);
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
        return new LibraryFunUse(this.funInfo);
    }

    @Override
    public String getName() {
        return this.funInfo.getDisplayName();
    }

    @Override
    public boolean isScopeCorrectIn(BlockContainer container) {
        return true;
    }

    @Override
    public void deleteLinks() {
    }

}
