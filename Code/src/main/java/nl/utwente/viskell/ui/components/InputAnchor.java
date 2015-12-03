package nl.utwente.viskell.ui.components;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Hole;
import nl.utwente.viskell.haskell.expr.LetExpression;
import nl.utwente.viskell.haskell.type.Type;
import nl.utwente.viskell.haskell.type.TypeScope;

import com.google.common.collect.ImmutableMap;

/**
 * ConnectionAnchor that specifically functions as an input.
 */
public class InputAnchor extends ConnectionAnchor {

    /** The visual representation of the InputAnchor. */
    @FXML private Shape visibleAnchor;
    
    /** The invisible part of the InputAnchor (the touch zone). */
    @FXML private Shape invisibleAnchor;

    /** The thing sticking out of an unconnected InputAnchor. */
    @FXML private Shape openWire;
    
    /** The Optional connection this anchor has. */
    private Optional<Connection> connection;
    
    /** The local type of this anchor */
    private Type type;
    
    /** Property storing the error state. */
    private BooleanProperty errorState;

    /** The pictogram to show when in error state. */
    private final static Image ERROR_PICTURE = new Image(InputAnchor.class.getResourceAsStream("/ui/warningTriangle.png"));
    
    /** The ImageView used to indicate a type mismatch. */
    private ImageView errorImage;
    
    /**
     * @param block
     *            The Block this anchor is connected to.
     */
    public InputAnchor(Block block) {
        super(block);
        this.loadFXML("InputAnchor");
        
        this.connection = Optional.empty();
        this.type = TypeScope.unique("???");
        
        this.errorImage = new ImageView(ERROR_PICTURE);
        double height = this.visibleAnchor.getBoundsInLocal().getHeight();
        double width = this.visibleAnchor.getBoundsInLocal().getWidth();
        this.errorImage.setFitHeight(height);
        this.errorImage.setFitWidth(width);
        this.errorImage.setMouseTransparent(true);
        this.errorImage.setVisible(false);
        this.getChildren().add(this.errorImage);
        
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
    
    /**
     * @param state The new error state for this ConnectionAnchor.
     */
    protected void setErrorState(boolean state) {
        this.errorState.set(state);
    }
    
    /**
     * @return The property describing the error state of this ConnectionAnchor.
     */
    public BooleanProperty errorStateProperty() {
        return errorState;
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
    
    @Override
    public Type getType() {
        return this.type;
    }
    
    /**
     * Sets the type constraint of this input anchor to a fresh type.
     * @param type constraint which this input anchor will require.
     * @param scope wherein the fresh type is constructed.
     */
    public void setFreshRequiredType(Type type, TypeScope scope) {
        this.type = type.getFresh(scope);
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
    public Pair<Expression, Set<Block>> getLocalExpr() {
        return new Pair<>(this.connection.map(c -> c.getStartAnchor().getVariable()).orElse(new Hole()), new HashSet<>());
    }
    
    /**
     * @return The full expression carried by the connection connected to this anchor.
     */
    public Expression getFullExpr() {
        Pair<Expression, Set<Block>> pair = this.getLocalExpr();
        LetExpression fullExpr = new LetExpression(pair.a, false);
        Set<Block> surroundingBlocks = pair.b;
        this.extendExprGraph(fullExpr, null, surroundingBlocks);
        
        surroundingBlocks.forEach(block -> block.extendExprGraph(fullExpr, null, new HashSet<>()));
        return fullExpr;
    }
    
    /**
     * Extends the expression graph to include all subexpression required
     * @param exprGraph the let expression representing the current expression graph
     * @param container the container to which this expression graph is constrained
     * @param addLater a mutable list of blocks that have to be added by a surrounding container
     */
    protected void extendExprGraph(LetExpression exprGraph, BlockContainer container, Set<Block> addLater) {
        this.connection.ifPresent(connection -> connection.extendExprGraph(exprGraph, container, addLater));
    }

    /**
     * ChangeListener that will set the error state if isConnected().
     */
    public void checkError(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        this.errorImage.setVisible(newValue);
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
