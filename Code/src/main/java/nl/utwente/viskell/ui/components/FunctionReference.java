package nl.utwente.viskell.ui.components;

import java.util.Optional;
import java.util.Set;

import javafx.scene.layout.Region;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.BlockContainer;

public interface FunctionReference {
    
    public void initializeBlock(Block funBlock);
    
    public Optional<InputAnchor> getInputAnchor();
    
    public int requiredArguments();
    
    public Type refreshedType(int argCount, TypeScope scope);
    
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors);

    public void invalidateVisualState();
    
    public Region asRegion();
    
    public FunctionReference getNewCopy();
    
    public String getName();
    
    public boolean isScopeCorrectIn(BlockContainer container);

}
