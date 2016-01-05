package nl.utwente.viskell.ui.components;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Hole;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;
import nl.utwente.viskell.ui.BlockContainer;

import com.google.common.collect.ImmutableMap;

/**
 * ConnectionAnchor that specifically functions as an input.
 */
public class InputAnchor extends ConnectionAnchor implements ConnectionAnchor.Target {

    /** The visual representation of the InputAnchor. */
    @FXML protected Shape visibleAnchor;
    
    /** The invisible part of the InputAnchor (the touch zone). */
    @FXML protected Shape invisibleAnchor;

    /** The thing sticking out of an unconnected InputAnchor. */
    @FXML protected Shape openWire;
    
    /** The Optional connection this anchor has. */
    private Optional<Connection> connection;
    
    /** The local type of this anchor */
    private Type type;
    
    /** Property storing the error state. */
    private BooleanProperty errorState;

    /**
     * @param block
     *            The Block this anchor is connected to.
     */
    public InputAnchor(Block block) {
        super(block);
        this.loadFXML("InputAnchor");
        
        this.connection = Optional.empty();
        this.type = TypeScope.unique("???");
        
        this.errorState = new SimpleBooleanProperty(false);
        this.errorState.addListener(this::checkError);
    }

    /** For use in subclasses only. */
    protected InputAnchor() {
        super(null);
        this.connection = Optional.empty();
        this.errorState = new SimpleBooleanProperty(false);
        this.errorState.addListener(this::checkError);
    }
    
    /**
     * @param block The Block this anchor is connected to.
     * @param type The type constraint for this anchor.
     */
    public InputAnchor(Block block, Type type) {
        this(block);
        this.type = type;
    }
    
    @Override
    public ConnectionAnchor getAssociatedAnchor() {
        return this;
    }
    
    /**
     * @param state The new error state for this ConnectionAnchor.
     */
    protected void setErrorState(boolean state) {
        this.errorState.set(state);
    }
    
    /** @return The Optional connection this anchor has. */
    public Optional<Connection> getConnection() {
        return this.connection;
    }

    /**
     * Sets the connection of this anchor.
     * @param connection The connection to set.
     */
    protected void setConnection(Connection connection) {
        this.connection = Optional.of(connection);
        this.openWire.setVisible(false);
    }
    
    @Override
    public void removeConnections() {
        if (this.connection.isPresent()) {
            Connection conn = this.connection.get();
            this.connection = Optional.empty();
            conn.remove();
        }
        this.setErrorState(false);
        this.openWire.setVisible(true);
    }

    @Override
    public boolean hasConnection() {
        return this.connection.isPresent();
    }
    
    /** @return True if this anchor has an error free connection */
    public boolean hasValidConnection() {
        return this.connection.isPresent() && ! (this.errorState.get() || this.connection.get().hasScopeError());
    }
    
    @Override
    public Point2D getAttachmentPoint() {
        return this.getPane().sceneToLocal(this.localToScene(new Point2D(0, -7)));
    }

    /**
     * @return the local type of this anchor
     */
    public Type getType() {
        return this.type;
    }
    
    @Override
    public Type getFreshType() {
        return this.type.getFresh();
    }

    /**
     * @return the string representation of the in- or output type.
     */
    public final String getStringType() {
        return this.getType().prettyPrint();
    }
    
    /**
     * Sets the type constraint of this input anchor to a fresh type.
     * @param type constraint which this input anchor will require.
     * @param scope wherein the fresh type is constructed.
     */
    public void setFreshRequiredType(Type type, TypeScope scope) {
        this.type = type.getFresh(scope);
    }

    public void setExactRequiredType(Type type) {
        this.type = type;
    }
    
    /**
     * @return Optional of the connection's opposite output anchor.
     */
    public Optional<OutputAnchor> getOppositeAnchor() {
        return this.connection.map(c -> c.getStartAnchor());
    }
    
    /**
     * @return The local expression carried by the connection connected to this anchor.
     */
    public Expression getLocalExpr(Set<OutputAnchor> outsideAnchors) {
        return this.connection.map(c -> c.getStartAnchor().getVariable()).orElse(new Hole());
    }
    
    /**
     * This function assumes that the function is in the top level container.
     * @return The full expression carried by the connection connected to this anchor.
     */
    public Expression getFullExpr() {
        Set<OutputAnchor> outsideAnchors = new HashSet<>();
        LetExpression fullExpr = new LetExpression(this.getLocalExpr(outsideAnchors), false);
        
        /**
         * Iterate over the container until it doesn't find new nodes.
         */
        boolean cont = true;
        for (int numAnchors = -1; numAnchors != outsideAnchors.size() || cont; numAnchors = outsideAnchors.size()) {
            cont = (numAnchors != outsideAnchors.size());
            extendExprGraph(fullExpr, block.container, outsideAnchors);
            outsideAnchors.forEach(connection -> connection.extendExprGraph(fullExpr, block.container, outsideAnchors));
        }
    
        return fullExpr;
    }
    
    /**
     * Extends the expression graph to include all subexpression required
     * @param exprGraph the let expression representing the current expression graph
     * @param container the container to which this expression graph is constrained
     * @param outsideAnchors a mutable set of required OutputAnchors from a surrounding container
     */
    protected void extendExprGraph(LetExpression exprGraph, BlockContainer container, Set<OutputAnchor> outsideAnchors) {
        connection.ifPresent(connection -> connection.extendExprGraph(exprGraph, container, outsideAnchors));
    }

    /**
     * ChangeListener that will set the error state if isConnected().
     */
    public void checkError(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        ObservableList<String> style = this.visibleAnchor.getStyleClass();
        style.removeAll("error");
        if (newValue) {
            style.add("error");
        }
    }

    @Override
    protected void setNearbyWireReaction(int goodness) {
        if (goodness > 0) {
            this.openWire.setStroke(Color.STEELBLUE);
            this.openWire.setStrokeWidth(5);
            this.visibleAnchor.setFill(Color.STEELBLUE);
        } else if (goodness < 0) {
            this.openWire.setStroke(Color.RED);
            this.openWire.setStrokeWidth(3);
            this.visibleAnchor.setFill(Color.RED);
        } else {
            this.openWire.setStroke(Color.BLACK);
            this.openWire.setStrokeWidth(3);
            this.visibleAnchor.setFill(Color.BLACK);
        }
    }
    
    @Override
    public void setWireInProgress(DrawWire wire) {
        super.setWireInProgress(wire);
        if (wire == null) {
            this.openWire.setVisible(!this.hasConnection());
            this.invisibleAnchor.setMouseTransparent(false);
        } else {
            this.openWire.setVisible(false);
            this.invisibleAnchor.setMouseTransparent(true);
        }
    }

    /** Called when the VisualState changed. */
    public void invalidateVisualState() {
    	this.connection.ifPresent(c -> c.invalidateVisualState());
    }

    @Override
    public BlockContainer getContainer() {
        return this.block.getContainer();
    }
    
    @Override
    public String toString() {
        return "InputAnchor for " + this.block;
    }

    @Override
    public Map<String, Object> toBundle() {
        ImmutableMap.Builder<String, Object> bundle = ImmutableMap.builder();
        bundle.put("startBlock", this.block.hashCode());
        bundle.put("startAnchor", 0);
        return bundle.build();
    }

}
