package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableMap;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import nl.utwente.viskell.haskell.env.FunctionInfo;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.FunVar;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.serialize.Bundleable;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LibraryFunUse extends Label implements FunctionReference {
    
    private final FunctionInfo funInfo;
    
    public LibraryFunUse(FunctionInfo funInfo) {
        super(funInfo.getDisplayName());
        this.funInfo = funInfo;
        this.setMinWidth(USE_PREF_SIZE);
        this.setMaxWidth(USE_PREF_SIZE);
        this.getStyleClass().add("title");
    }

    public Map<String, Object> toBundleFragment() {
        return ImmutableMap.of(
                Bundleable.KIND, this.getClass().getSimpleName(),
                "funInfo", this.funInfo.toBundleFragment()
        );
    }

    public static LibraryFunUse fromBundleFragment(Map<String, Object> bundleFragment) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> funInfoBundle = (Map<String, Object>)bundleFragment.get("funInfo");
        FunctionInfo funInfo = FunctionInfo.fromBundleFragment(funInfoBundle);
        return new LibraryFunUse(funInfo);
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
