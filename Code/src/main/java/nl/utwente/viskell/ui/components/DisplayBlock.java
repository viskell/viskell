package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.expr.*;
import nl.utwente.viskell.haskell.type.*;
import nl.utwente.viskell.ui.ToplevelPane;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * DisplayBlock is an extension of {@link Block} that only provides a display of
 * the input it receives through it's {@link InputAnchor}. The input will be
 * rendered visually on the Block. DisplayBlock can be empty and contain no
 * value at all, the value can be altered at any time by providing a different
 * input source using a {@link Connection}.
 */
public class DisplayBlock extends Block implements ConnectionAnchor.Target {

    /** The Anchor that is used as input. */
    protected InputAnchor inputAnchor;

    /** The space containing the input anchor. */
    @FXML protected Pane inputSpace;

    /** The label on which to display the value of this block */
    @FXML protected Label value;
    
    /** Show class constrained type variable for the input anchor */
    private final Type showConstraint;
            
    /**
     * Creates a new instance of DisplayBlock.
     * @param pane
     *            The pane on which this DisplayBlock resides.
     */
    public DisplayBlock(ToplevelPane pane) {
        this(pane, "DisplayBlock");
    }

    @SuppressWarnings("UnusedParameters")
    public static DisplayBlock fromBundleFragment(ToplevelPane pane, Map<String, Object> bundleFragment) {
        return new DisplayBlock(pane);
    }

    protected DisplayBlock(ToplevelPane pane, String fxml) {
        super(pane);
        this.showConstraint = pane.getEnvInstance().buildType("Show a => a");
        loadFXML(fxml);

        inputAnchor = new InputAnchor(this);
        inputSpace.getChildren().add(0, inputAnchor);
    }

    @Override
    public void invalidateVisualState() {
        this.inputAnchor.invalidateVisualState();

        if (this.inValidContext && inputAnchor.hasValidConnection()) {
            try { 
                TypeChecker.unify("is showable", inputAnchor.getType().getFresh(), showConstraint.getFresh());
            
                GhciSession ghci = getToplevel().getGhciSession();

                Expression expr = inputAnchor.getFullExpr();
                Type type = inputAnchor.getType().getConcrete();
                if (type instanceof TypeApp) {
                    List<Type> tapps = ((TypeApp)type).asFlattenedAppChain();
                    if (tapps.get(0) instanceof ListTypeCon) {
                        // add an extra take on lists, so we don't try to fully eval infinite ones
                        FunVar take = new FunVar(this.getToplevel().getEnvInstance().lookupFun("take"));
                        expr = new Apply (new Apply(take, new Value(Type.con("Int"), "32")), expr);
                    }
                }
                
                ListenableFuture<String> result = ghci.pull(expr);

                Futures.addCallback(result, new FutureCallback<String>() {
                    public void onSuccess(String s) {
                        // Can't call setOutput directly - this may not be JavaFX app thread.
                        // Instead, schedule setting the output.
                        Platform.runLater(() -> value.setText(s));
                    }

                    public void onFailure(Throwable throwable) {
                        if (throwable instanceof HaskellException && "Open expression".equals(throwable.getMessage())) {
                            Platform.runLater(() -> value.setText("unfinished?"));
                        } else {
                            Platform.runLater(() -> value.setText("?!?!?!"));
                        }
                    }
                });

            } catch (HaskellTypeError e) {
                value.setText("_ :: " + inputAnchor.getStringType());
            }
            
        } else {
            value.setText("?");
        }
    }
    
    //TODO NOTE: only used for a meaningless test
    public String getOutput() {
        return value.getText();
    }
    
    @Override
    public ConnectionAnchor getAssociatedAnchor() {
        return inputAnchor;
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(inputAnchor);
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of();
    }
    
    @Override
    public Optional<Block> getNewCopy() {
        return Optional.of(new DisplayBlock(this.getToplevel()));
    }
    
    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        return inputAnchor.getLocalExpr(outsideAnchors);
    }
    
    @Override
    public void refreshAnchorTypes() {
        this.inputAnchor.setFreshRequiredType(TypeScope.unique("q"), new TypeScope());        
    }

    @Override
    public String toString() {
        return "DisplayBlock[" + value.getText() + "]";
    }

    @Override
    public boolean checkValidInCurrentContainer() {
        if (this.container instanceof LambdaContainer || this.container instanceof Lane) {
            return false;
        } else {
            return super.checkValidInCurrentContainer();
        }
    }

}
