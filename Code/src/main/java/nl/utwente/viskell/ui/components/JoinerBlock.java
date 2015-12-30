package nl.utwente.viskell.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Value;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ToplevelPane;

/** A compact wire joining/tuple constructing block. */
public class JoinerBlock extends Block {
    
    /** A list of InputAnchor for every tuple element. */
    private final List<InputAnchor> inputs;
    
    /** The output anchor with the result tuple. */
    private final OutputAnchor output;

    public JoinerBlock(ToplevelPane pane, int arity) {
        super(pane);
        this.output = new OutputAnchor(this, new Binder("tuple_"+arity));
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
        Polygon joiner = new Polygon(
                -xbase-4*xstep, 0*ystep,  xbase+4*xstep, 0*ystep,
                 xbase+3*xstep, 1*ystep,  xbase+1*xstep, 2*ystep,
                 xbase+0*xstep, 3*ystep, -xbase-0*xstep, 3*ystep,
                -xbase-1*xstep, 2*ystep, -xbase-3*xstep, 1*ystep);
        joiner.setFill(Color.MIDNIGHTBLUE);
        joiner.setPickOnBounds(true);
        
        VBox body = new VBox(inputSpace, joiner, outputSpace);
        this.getChildren().add(body);
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
        List<Type> elems = new ArrayList<>();
        TypeScope scope = new TypeScope();
        for (InputAnchor input : this.inputs) {
            input.setFreshRequiredType(TypeScope.unique("e"), scope);
            elems.add(input.getType());
        }
        
        this.output.setExactRequiredType(Type.tupleOf(elems.toArray(new Type[elems.size()])));
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        // sometimes textual code generation is just easier
        StringBuilder expr = new StringBuilder();
        expr.append("(");
        for (InputAnchor input : this.inputs) {
            expr.append(input.getLocalExpr(outsideAnchors).toHaskell());
            expr.append(",");
        }
        expr.setCharAt(expr.length()-1, ')');
        
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
        return Optional.of(new JoinerBlock(this.getToplevel(), this.inputs.size()));
    }

}
