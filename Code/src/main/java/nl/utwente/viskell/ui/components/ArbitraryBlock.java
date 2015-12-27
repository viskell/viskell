package nl.utwente.viskell.ui.components;

import java.util.Optional;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.ToplevelPane;

/** This variant of the ValueBlock uses QuickCheck to generate values based on the output type. */
public class ArbitraryBlock extends ValueBlock {
    
    /** The button for getting the next randomly generated value */
    @FXML private Button rngTrigger; 
    
    /** Whether a value has been generated for this block. */
    private boolean hasValue;
    
    /**
     * Constructs a new ArbitraryBlock
     * @param pane The parent pane this Block resides on.
     */
    public ArbitraryBlock(ToplevelPane pane) {
        super("ArbitraryBlock", pane, pane.getEnvInstance().buildType("Arbitrary a => a"));
        this.rngTrigger.setOnAction(event -> this.getNextValue(event.hashCode()));
        this.hasValue = false;
        this.getNextValue(this.hashCode());
    }

    private void getNextValue(int seed) {
        this.setValue("?????");
        this.hasValue = false;
        
        if (! this.output.hasConnection()) {
            return;
        }

        // we cannot generate values for polymorphic types and we don't try to for function types
        Type type = this.output.getType().getConcrete();
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
        this.hasValue = true;
        GhciSession ghci = this.getToplevel().getGhciSession();
        int genOffset = Math.abs(seed) % 10;
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
                Platform.runLater(() -> ArbitraryBlock.this.setValue("....."));
            }
        });
    }
    
    @Override
    public void invalidateVisualState() {
        if (this.hasValue != this.output.hasConnection()) {
            // attempt to refresh the value
            this.getNextValue(5);
        }

        super.invalidateVisualState();
    }
    
    @Override
    public Optional<Block> getNewCopy() {
        return Optional.of(new ArbitraryBlock(this.getToplevel()));
    }

}
