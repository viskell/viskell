package nl.utwente.viskell.ui.components;

import com.google.common.collect.ImmutableMap;
import javafx.geometry.Point2D;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.BlockContainer;
import nl.utwente.viskell.ui.serialize.Bundleable;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

public class ApplyAnchor extends InputAnchor implements FunctionReference {

    private int baseArity;
    
    public ApplyAnchor(int baseArity) {
        super(); // block is to be set in initializeBlock
        this.baseArity = baseArity;
        this.setMinSize(60, 24);
        this.setMaxSize(60, 24);
        this.setPickOnBounds(false);
        this.invisibleAnchor = new Rectangle(40, 40);
        this.invisibleAnchor.getStyleClass().add("invisTouchZone");
        this.invisibleAnchor.setTranslateX(-10);
        this.invisibleAnchor.setTranslateY(-20);
        this.visibleAnchor = new Rectangle(48, 12);
        this.visibleAnchor.setTranslateY(3);
        this.visibleAnchor.getStyleClass().add("anchorPoint");
        this.visibleAnchor.setMouseTransparent(true);
        this.openWire = new Line(0, 0, 0, 8);
        this.openWire.setTranslateX(-8);
        this.openWire.setTranslateY(-8);
        this.openWire.setStrokeWidth(6);
        this.getChildren().addAll(this.invisibleAnchor, this.visibleAnchor, this.openWire);
        this.refreshedType(baseArity, new TypeScope());
    }

    public Map<String, Object> toBundleFragment() {
        return ImmutableMap.of(
                Bundleable.KIND, this.getClass().getSimpleName(),
                "arity", this.baseArity
        );
    }

    public static ApplyAnchor fromBundleFragment(Map<String, Object> bundleFragment) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int arity = ((Double)bundleFragment.get("arity")).intValue();
        return new ApplyAnchor(arity);
    }

    @Override
    public void initializeBlock(Block funBlock) {
        this.block = funBlock;
    }

    @Override
    public Optional<InputAnchor> getInputAnchor() {
        return Optional.of(this);
    }

    @Override
    public Point2D getAttachmentPoint() {
        return this.getPane().sceneToLocal(this.localToScene(new Point2D(22, 8)));
    }
    
    @Override
    public int requiredArguments() {
        if (this.hasConnection()) {
            return this.getType().countArguments();
        } else {
            return this.baseArity;
        }
    }

    @Override
    public Type refreshedType(int argCount, TypeScope scope) {
        if (argCount < 1) {
            return scope.getVar("xr");
        }

        Type res = Type.fun(scope.getVar("a_0"), scope.getVar("xr"));
        for (int i = argCount-1; i > 0 ; i--) {
            res = Type.fun(scope.getVar("a_"+i), res);
        }

        this.setFreshRequiredType(res, scope);
        return res;
    }

    @Override
    public Region asRegion() {
        return this;
    }

    @Override
    public FunctionReference getNewCopy() {
        return new ApplyAnchor(this.requiredArguments());
    }

    @Override
    public String getName() {
        return "@pply_" + this.hashCode();
    }

    @Override
    public boolean isScopeCorrectIn(BlockContainer container) {
        return true; // don't care for now, scope errors are already shown on the anchor itself 
    }

    @Override
    public void deleteLinks() {
        this.removeConnections();
    }

    
}
