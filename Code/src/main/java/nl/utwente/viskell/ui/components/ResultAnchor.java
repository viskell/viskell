package nl.utwente.viskell.ui.components;

import java.util.Optional;
import java.util.Set;

import javafx.scene.control.Label;
import nl.utwente.viskell.haskell.expr.Annotated;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.BlockContainer;

/** An internal input anchor for a local result. */
public class ResultAnchor extends InputAnchor {
    
    private final WrappedContainer container;
    
    /** The optional type of the result of the function (the last part of the signature). */
    private Optional<Type> resType;
    
    /** The type label of this anchor. */
    private final Label typeLabel;
    
    // FIXME ResultAnchor should not have or use the DefinitionBlock parent
    public ResultAnchor(WrappedContainer container, Block parent, Optional<Type> resType) {
        super(parent);
        this.container = container;
        this.resType = resType;
        this.typeLabel = new Label(".....");
        this.typeLabel.setMinWidth(USE_PREF_SIZE);
        this.typeLabel.setPickOnBounds(false);
        this.typeLabel.setMouseTransparent(true);
        this.typeLabel.getStyleClass().add("resultType");
        this.typeLabel.setTranslateY(9);
        this.getChildren().add(this.typeLabel);
        this.setTranslateY(-9);
    }
    
    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        if (resType.isPresent()) {
            return new Annotated(super.getLocalExpr(outsideAnchors), resType.get());
        }
       
        return super.getLocalExpr(outsideAnchors);
    }
    
    protected void setConstraintType(Type ctype) {
        this.resType = Optional.of(ctype);
    }
    
    /** Set fresh type for the next typechecking cycle.*/
    protected void refreshAnchorType(TypeScope scope) {
        if (resType.isPresent()) {
            setFreshRequiredType(resType.get(), scope);
        } else {
            setFreshRequiredType(new TypeScope().getVar("r"), scope);
        }
    }

    @Override
    protected void handleConnectionChanges(boolean finalPhase) {
        container.handleConnectionChanges(finalPhase);
    }
    
    @Override
    public BlockContainer getContainer() {
        return this.container;
    }
    
    @Override
    public void invalidateVisualState() {
        super.invalidateVisualState();
        boolean validConnection = this.hasValidConnection();
        this.setTranslateY(validConnection ? 0 : -9);
        this.typeLabel.setText(validConnection ? "zyxwv" : this.getStringType()); 
        this.typeLabel.setVisible(!validConnection);
    }
    
}
