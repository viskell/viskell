package nl.utwente.viskell.ui.components;

import java.util.Optional;

import javafx.scene.control.TextInputDialog;
import nl.utwente.viskell.ghcj.GhciSession;
import nl.utwente.viskell.ghcj.HaskellException;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ToplevelPane;

/** Block with a constant value that is editable as a plain text expression. */
public class ConstantBlock extends ValueBlock {

    private boolean hasValidValue;
    
    public ConstantBlock(ToplevelPane pane) {
        super("ValueBlock", pane, TypeScope.unique("x"));
        this.setValue("undefined");
        this.hasValidValue = false;
        this.outputSpace.setVisible(false);
    }
    
    public ConstantBlock(ToplevelPane pane, Type type, String value, boolean hasValidValue) {
        super("ValueBlock", pane, type);
        this.setValue(value);
        this.hasValidValue = hasValidValue;
    }

    public void editValue(Optional<String> startValue) {
        TextInputDialog dialog = new TextInputDialog(startValue.orElse(this.getValue()));
        dialog.setTitle("Edit constant block");
        dialog.setHeaderText("Type a Haskell expression");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(value -> {
            this.setValue(value);
            GhciSession ghci = this.getToplevel().getGhciSession();

            try {
                Type type = ghci.pullType(value, this.getToplevel().getEnvInstance());
                this.output.setExactRequiredType(type);
                this.hasValidValue = true;
                this.outputSpace.setVisible(true);
            } catch (HaskellException e) {
                this.hasValidValue = false;
                this.outputSpace.setVisible(false);
            }
            
            this.initiateConnectionChanges();
        });
    }

    @Override
    public ConnectionAnchor getAssociatedAnchor() {
        if (hasValidValue) {
            return output;
        } else {
            return null;
        }
    }
    
    @Override
    public Optional<Block> getNewCopy() {
        return Optional.of(new ConstantBlock(this.getToplevel(), this.output.binder.getFreshAnnotationType(), this.value.getText(), this.hasValidValue));
    }

}
