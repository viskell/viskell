package nl.utwente.viskell.ui.components;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nl.utwente.viskell.haskell.expr.Expression;
import nl.utwente.viskell.haskell.expr.Hole;
import nl.utwente.viskell.haskell.expr.LetExpression;

/**
 * ConnectionAnchor that specifically functions as an input.
 */
public class InputAnchor extends ConnectionAnchor {
    /** The Optional connection this anchor has. */
    private Optional<Connection> connection;
    
    /** The expression to return when there is no connection. */
    private Hole connectionlessExpr;
    
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
        this.connection = Optional.empty();
        this.connectionlessExpr = new Hole();
        
        this.errorImage = new ImageView(ERROR_PICTURE);
        double height = this.getVisibleAnchor().getBoundsInLocal().getHeight();
        double width = this.getVisibleAnchor().getBoundsInLocal().getWidth();
        this.errorImage.setFitHeight(height);
        this.errorImage.setFitWidth(width);
        this.errorImage.setMouseTransparent(true);
        this.errorImage.setVisible(false);
        this.getChildren().add(this.errorImage);
        
        this.errorState = new SimpleBooleanProperty(false);
        this.errorState.addListener(this::checkError);
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
    }
    
    @Override
    public void removeConnections() {
        if (this.connection.isPresent()) {
            Connection conn = this.connection.get();
            this.connection = Optional.empty();
            conn.remove();
        }
        this.setErrorState(false);
    }

    @Override
    public boolean hasConnection() {
        return this.connection.isPresent();
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
    public Expression getLocalExpr() {
        return this.connection.map(c -> c.getStartAnchor().getVariable()).orElse(connectionlessExpr);
    }
    
    /**
     * @return The full expression carried by the connection connected to this anchor.
     */
    public Expression getFullExpr() {
        LetExpression expr = new LetExpression(this.getLocalExpr());
        this.extendExprGraph(expr);
        return expr;
    }
    
    /**
     * Extends the expression graph to include all subexpression required
     * @param exprGraph the let expression representing the current expression graph
     */
    protected void extendExprGraph(LetExpression exprGraph) {
        this.connection.ifPresent(c -> c.getStartAnchor().extendExprGraph(exprGraph));
    }

    /**
     * Gets the Expression that is connected to this, or when not connected create a fresh expression representing the open input.   
     * @return The updated expression carried by the connection connected to this anchor.
     */
    public final Expression getUpdatedExpr() {
        this.connectionlessExpr = new Hole();
        return this.getLocalExpr();
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
