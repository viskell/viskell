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

public class ArbitraryBlock extends ValueBlock {
    
    @FXML private Button rngTrigger; 
    
    /**
     * Constructs a new ArbitraryBlock
     * @param pane The parent pane this Block resides on.
     */
    public ArbitraryBlock(ToplevelPane pane) {
        super("ArbitraryBlock", pane, pane.getEnvInstance().buildType("Arbitrary a => a"));
        this.rngTrigger.setOnAction(event -> this.getNextValue(event.hashCode()));
        this.getNextValue(this.hashCode());
    }

    private void getNextValue(int seed) {
        this.setValue("?????");
        
        if (! this.output.hasConnection()) {
            return;
        }

        // we cannot generate values for complicated types
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
        if (this.value.getText().startsWith("?")) {
            // try again to get a value
            this.getNextValue(5);
        }

        super.invalidateVisualState();
    }
    
    @Override
    public Optional<Block> getNewCopy() {
        return Optional.of(new ArbitraryBlock(this.getToplevel()));
    }

}
