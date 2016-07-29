package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.ui.ToplevelPane;

/** A compact Monoid append block. */
public class AppendBlock extends Block {
    /** A list of InputAnchor for each input. */
    private final List<InputAnchor> inputs;
    
    /** The output anchor with the monoid append result. */
    private final OutputAnchor output;

    /** Monoid class constrained type variable for the anchors. */
    private final Type monoidConstraint;

    
    public AppendBlock(ToplevelPane pane, int arity) {
        super(pane);
        this.monoidConstraint = pane.getEnvInstance().buildType("Monoid m => m");

        this.output = new OutputAnchor(this, new Binder("append_"+arity));
        this.inputs = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            this.inputs.add(new InputAnchor(this));
        }

        HBox inputSpace = new HBox(40);
        inputSpace.getChildren().addAll(this.inputs);
        inputSpace.setPickOnBounds(false);
        inputSpace.setAlignment(Pos.CENTER);
        HBox outputSpace = new HBox(this.output);
        outputSpace.setPickOnBounds(false);
        outputSpace.setAlignment(Pos.CENTER);

        int xbase = 10;              // = anchor_width / 2
        int xstep = (arity - 1) * 5; // 5 = horizontal_spacing / 8
        int ystep = 8;               // = height / 3
        Polygon appender = new Polygon(
                -xbase-4*xstep, 0*ystep,    xbase+4*xstep, 0*ystep,
               3+xbase+4*xstep, 1*ystep,    xbase+0*xstep, 3*ystep,
                -xbase-0*xstep, 3*ystep, -3-xbase-4*xstep, 1*ystep);
        appender.setFill(Color.SADDLEBROWN);
        appender.setPickOnBounds(true);
        
        VBox body = new VBox(inputSpace, appender, outputSpace);
        body.setPickOnBounds(false);
        this.getChildren().add(body);
        this.setPickOnBounds(false);
    }

    /** @return class-specific properties of this Block. */
    @Override
    protected Map<String, Object> toBundleFragment() {
        return ImmutableMap.of("arity", this.inputs.size());
    }

    public static AppendBlock fromBundleFragment(ToplevelPane pane, Map<String,Object> bundleFragment) {
        int arity = ((Double)bundleFragment.get("arity")).intValue();
        return new AppendBlock(pane, arity);
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return inputs;
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return ImmutableList.of(this.output);
    }

    @Override
    protected void refreshAnchorTypes() {
        Type monoidal = this.monoidConstraint.getFresh();
        for (InputAnchor input : this.inputs) {
            input.setExactRequiredType(monoidal);
        }
        
        this.output.setExactRequiredType(monoidal);
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        // sometimes textual code generation is just easier
        StringBuilder expr = new StringBuilder();
        expr.append("(");
        for (int i = 0; i < this.inputs.size(); i++) {
            if (i != 0) {
                expr.append(" `mappend` ");
            }
            InputAnchor input = this.inputs.get(i);
            expr.append(input.getLocalExpr(outsideAnchors).toHaskell());
        }
        expr.append(')');
        
        return new Value(this.output.getType(Optional.empty()).getFresh(), expr.toString());
    }

    @Override
    public void invalidateVisualState() {
        for (InputAnchor input : this.inputs) {
            input.invalidateVisualState();
        }
    }

    @Override
    public Optional<Block> getNewCopy() {
        return Optional.of(new AppendBlock(this.getToplevel(), this.inputs.size()));
    }

}
