package nl.utwente.viskell.ui.components;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.scene.layout.Region;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.BlockContainer;

public interface FunctionReference {
    
    void initializeBlock(Block funBlock);
    
    Optional<InputAnchor> getInputAnchor();
    
    int requiredArguments();
    
    Type refreshedType(int argCount, TypeScope scope);
    
    Expression getLocalExpr(Set<OutputAnchor> outsideAnchors);

    void invalidateVisualState();
    
    Region asRegion();
    
    FunctionReference getNewCopy();
    
    String getName();
    
    boolean isScopeCorrectIn(BlockContainer container);

    void deleteLinks();

    Map<String, Object> toBundleFragment();
}
