package nl.utwente.viskell.ui.components;

import java.util.*;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableMap;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import nl.utwente.viskell.haskell.expr.Binder;
import nl.utwente.viskell.haskell.expr.ConstructorBinder;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.type.TupleTypeCon;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeApp;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.ToplevelPane;

public class SplitterBlock extends Block {

    /** The InputAnchor of the input tuple. */
    private final InputAnchor input;
    
    /** A list of OutputAnchors for every tuple element. */
    private final List<OutputAnchor> outputs;
    
    /** The pattern matching binder for the whole output. */
    private ConstructorBinder primaryBinder;
    
    public SplitterBlock(ToplevelPane pane, int arity) {
        super(pane);
        this.input = new InputAnchor(this);
        this.outputs = new ArrayList<>();
        List<Binder> elemBinders = new ArrayList<>();        
        for (int i = 0; i < arity; i++) {
            Binder eb = new Binder("e_"+i);
            elemBinders.add(eb);
            this.outputs.add(new OutputAnchor(this, eb));
        }
        this.primaryBinder = new ConstructorBinder(TupleTypeCon.tupleName(arity), elemBinders);
        
        HBox inputSpace = new HBox(this.input);
        inputSpace.setPickOnBounds(false);
        inputSpace.setAlignment(Pos.CENTER);
        HBox outputSpace = new HBox(40);
        outputSpace.getChildren().addAll(this.outputs);
        outputSpace.setPickOnBounds(false);
        outputSpace.setAlignment(Pos.CENTER);
        
        int xbase = 10;              // = anchor_width / 2
        int xstep = (arity - 1) * 5; // 5 = horizontal_spacing / 8
        int ystep = 8;               // = height / 3
        Polygon splitter = new Polygon(
                -xbase-4*xstep, 3*ystep,  xbase+4*xstep, 3*ystep,
                 xbase+3*xstep, 2*ystep,  xbase+1*xstep, 1*ystep,
                 xbase+0*xstep, 0*ystep, -xbase-0*xstep, 0*ystep,
                -xbase-1*xstep, 1*ystep, -xbase-3*xstep, 2*ystep);
        splitter.setFill(Color.MIDNIGHTBLUE);
        splitter.setPickOnBounds(true);
        
        VBox body = new VBox(inputSpace, splitter, outputSpace);
        body.setPickOnBounds(false);
        this.getChildren().add(body);
        this.setPickOnBounds(false);
    }

    /** @return class-specific properties of this Block. */
    @Override
    protected Map<String, Object> toBundleFragment() {
        return ImmutableMap.of("arity", this.outputs.size());
    }

    public static SplitterBlock fromBundleFragment(ToplevelPane pane, Map<String,Object> bundleFragment) {
        int arity = ((Double)bundleFragment.get("arity")).intValue();
        return new SplitterBlock(pane, arity);
    }

    @Override
    public List<InputAnchor> getAllInputs() {
        return ImmutableList.of(input);
    }

    @Override
    public List<OutputAnchor> getAllOutputs() {
        return this.outputs;
    }

    @Override
    protected void refreshAnchorTypes() {
        List<Type> elems = new ArrayList<>();
        for (int i = 0; i < this.outputs.size(); i++) {
            elems.add(TypeScope.unique("e_"+i));
        }

        this.input.setFreshRequiredType(Type.tupleOf(elems.toArray(new Type[elems.size()])), new TypeScope());
        List<Type> tupleElems = ((TypeApp)this.input.getType()).asFlattenedAppChain();
        tupleElems.remove(0);
        
        for (int i = 0; i < this.outputs.size(); i++) {
            this.outputs.get(i).setExactRequiredType(tupleElems.get(i));
        }
    }

    @Override
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        return this.input.getLocalExpr(outsideAnchors);
    }

    @Override
    public void invalidateVisualState() {
        this.input.invalidateVisualState();

    }

    @Override
    public Optional<Block> getNewCopy() {
        return Optional.of(new SplitterBlock(this.getToplevel(), this.outputs.size()));
    }

    public Binder getPrimaryBinder() {
        return primaryBinder;
    }
    
}
