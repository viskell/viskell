package nl.utwente.viskell.ui.components;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.ToplevelPane;
import nl.utwente.viskell.ui.serialize.Bundleable;

/** This variant of the ValueBlock uses QuickCheck to generate values based on the output type. */
public class ArbitraryBlock extends ValueBlock implements Bundleable {
    
    /** The button for getting the next randomly generated value */
    @FXML private Button rngTrigger; 
    
    /** The last type a for which a value has been generated, or empty is this block has no value. */
    private Optional<Type> lastGenType;
    
    /**
     * Constructs a new ArbitraryBlock
     * @param pane The parent pane this Block resides on.
     */
    public ArbitraryBlock(ToplevelPane pane) {
        super("ArbitraryBlock", pane, pane.getEnvInstance().buildType("Arbitrary a => a"));
        this.rngTrigger.setOnAction(event -> this.getNextValue(event.hashCode(), true));
        this.lastGenType = Optional.empty();
        this.output.refreshType(new TypeScope());
        this.getNextValue(this.hashCode(), false);
    }

    @Override
    protected Map<String, Object> toBundleFragment() {
        return ImmutableMap.of("value", getValue());
    }

    public static ArbitraryBlock fromBundleFragment(ToplevelPane pane, Map<String,Object> bundleFragment) {
        ArbitraryBlock arbitraryBlock = new ArbitraryBlock(pane);
        arbitraryBlock.setValue((String)bundleFragment.get("value"));
        return arbitraryBlock;
    }

    private void getNextValue(int seed, boolean fromClick) {
        Type outputType = this.output.getType(Optional.empty());
        if (! this.output.hasConnection()) {
            this.setValue("??");
            this.lastGenType = Optional.empty();
            return;
        }

        if (outputType instanceof TypeVar) {
            TypeVar tv = (TypeVar)outputType;    
            if (!(tv.hasConcreteInstance() || tv.getConstraints().count() > 1)) {
                this.setValue("???");
                this.lastGenType = Optional.empty();
                return;
            }
        }

        Optional<Type> defType = outputType.getFresh().defaultedConcreteType(Type.con("Bool"));
        Type type = defType.orElse(outputType);
        
        if (this.lastGenType.isPresent() && !fromClick) {
            try {
                TypeChecker.unify("arbitrary type changed", this.lastGenType.get().getFresh(), type.getFresh());
                // no incompatible type change, keep current value
                return;
            } catch (HaskellTypeError e) {
                // time for a new generated value
            }
        }
       
        
        this.setValue("???");
        this.lastGenType = Optional.empty();

        // we cannot generate values for polymorphic types and we don't try to for function types
        if (type instanceof TypeVar || type instanceof FunType) {
            return;
        } else if (type instanceof TypeApp) {
            for (Type xt : ((TypeApp)type).asFlattenedAppChain()) {
                if (! (xt instanceof TypeCon)) {
                    return;
                }
            }
        }
        
        // now let QuickCheck try to generate an arbitrary value of this type
        this.lastGenType = Optional.of(type);
        GhciSession ghci = this.getToplevel().getGhciSession();
        int genOffset = 2 + Math.abs(seed) % 7;
        String haskellType = type.prettyPrint(10);
        ListenableFuture<String> result = ghci.pullRaw("fmap (!!" + genOffset + ") $ sample' (arbitrary :: Gen " + haskellType + ")");

        Futures.addCallback(result, new FutureCallback<String>() {
            public void onSuccess(String s) {
                // Can't call setOutput directly - this may not be JavaFX app thread.
                // Instead, schedule setting the output.
                Platform.runLater(() -> {
                    ArbitraryBlock.this.setValue(s);
                    // propagate the new generated value
                    ArbitraryBlock.this.initiateConnectionChanges();
                });
            }

            public void onFailure(Throwable throwable) {
                Platform.runLater(() -> ArbitraryBlock.this.setValue("..."));
            }
        });
    }
    
    @Override
    public void invalidateVisualState() {
        this.getNextValue(5, false);
        super.invalidateVisualState();
    }
    
    @Override
    public Optional<Block> getNewCopy() {
        return Optional.of(new ArbitraryBlock(this.getToplevel()));
    }

}
