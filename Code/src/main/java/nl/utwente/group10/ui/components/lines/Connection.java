package nl.utwente.group10.ui.components.lines;

import java.util.Optional;

import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.hindley.GenSet;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.CustomUIPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;

/**
 * This is a ConnectionLine that also stores a startAnchor and an endAnchor to
 * keep track of origin points of the Line.
 *
 * For Lines that connect inputs and outputs of Blocks see Connection.
 */
public class Connection extends ConnectionLine implements
        ChangeListener<Number> {
    /** Starting point of this Line that can be Anchored onto other objects */
    private Optional<OutputAnchor> startAnchor = Optional.empty();
    /** Ending point of this Line that can be Anchored onto other objects */
    private Optional<InputAnchor> endAnchor = Optional.empty();
    /** Error indication Line */
    private Optional<Line> errorLine = Optional.empty();
    
    public Connection() {
        // Allow default constructor
    }

    public Connection(OutputAnchor from) {
        this.setStartAnchor(from);
        setEndPosition(getScenePoint(from));
    }

    public Connection(InputAnchor to) {
        this.setEndAnchor(to);
        setStartPosition(getScenePoint(to));
    }

    public Connection(OutputAnchor from, InputAnchor to) {
        this.setStartAnchor(from);
        this.setEndAnchor(to);
    }

    public Connection(InputAnchor to, OutputAnchor from) {
        this(from, to);
    }

    /**
     * Sets the free ends (empty anchors) to the specified position
     */
    public void setFreeEnds(double x, double y) {
        if (!startAnchor.isPresent()) {
            setStartPosition(x, y);
        }
        if (!endAnchor.isPresent()) {
            setEndPosition(x, y);
        }
    }

    /**
     * Tries to add an unspecified ConnectionAnchor to the connection.
     *
     * @param anchor Anchor to add
     * @param override If set will override (possible) existing Anchor.
     * @return Whether or not the anchor was added.
     */
    public boolean addAnchor(ConnectionAnchor anchor, boolean override) {
        boolean added = false;
        if ((!startAnchor.isPresent() || override) && anchor instanceof OutputAnchor) {
            disconnect(startAnchor);
            setStartAnchor((OutputAnchor) anchor);
            added = true;
        } else if ((!endAnchor.isPresent() || override) && anchor instanceof InputAnchor) {
            disconnect(endAnchor);
            setEndAnchor((InputAnchor) anchor);
            added = true;
        }

        return added;
    }

    public boolean addAnchor(ConnectionAnchor anchor) {
        return addAnchor(anchor, false);
    }

    /**
     * Set the startAnchor for this line. After setting the StartPosition will
     * be updated.
     *
     * @param start The OutputAnchor to start at.
     */
    public void setStartAnchor(OutputAnchor start) {
        setAnchor(startAnchor, start);
    }

    /**
     * Set the endAnchor for this line. After setting the EndPosition will be
     * updated.
     *
     * @param end the InputAnchor to end at.
     */
    public void setEndAnchor(InputAnchor end) {
        setAnchor(endAnchor, end);
    }

    private void setAnchor(Optional anchor, ConnectionAnchor newAnchor) {
        newAnchor.setConnection(this);
        newAnchor.getBlock().layoutXProperty().addListener(this);
        newAnchor.getBlock().layoutYProperty().addListener(this);

        if (newAnchor instanceof OutputAnchor) {
            if (startAnchor.isPresent()) {
                startAnchor.get().getBlock().layoutXProperty().removeListener(this);
                startAnchor.get().getBlock().layoutYProperty().removeListener(this);
            }
            startAnchor = Optional.of((OutputAnchor)newAnchor);
        } else if (newAnchor instanceof InputAnchor) {
            if (endAnchor.isPresent()) {
                endAnchor.get().getBlock().layoutXProperty().removeListener(this);
                endAnchor.get().getBlock().layoutYProperty().removeListener(this);
            }
            endAnchor = Optional.of((InputAnchor)newAnchor);
        } 
        
        checkError();
        updateStartEndPositions();
    }

    /**
     * Runs both the update Start end End position functions. Use when
     * refreshing UI representation of the Line.
     */
    private void updateStartEndPositions() {
        updateStartPosition();
        updateEndPosition();
    }

    /**
     * Refresh the Start position of this Line using startAnchor as a reference
     * point.
     */
    private void updateStartPosition() {
        if (startAnchor.isPresent()) {
            setStartPosition(getScenePoint(startAnchor.get()));
        }
        updateErrorLines();
    }
    
    /**
     * Refresh the End position of this Line using endAnchor as a reference
     * point.
     */
    private void updateEndPosition() {
        if (endAnchor.isPresent()) {
            setEndPosition(getScenePoint(endAnchor.get()));
        }
        updateErrorLines();
    }

    /** @return the scene-relative Point location of this anchor. */
    private Point2D getScenePoint(ConnectionAnchor anchor) {
        double x = anchor.getCenterX();
        double y = anchor.getCenterY();
        return anchor.localToScene(x, y);
    }

    /** @return this connection's start anchor, if any. */
    public final Optional<OutputAnchor> getOutputAnchor() {
        return startAnchor;
    }

    /** @return the upstream block, if any. */
    public final Optional<Block> getOutputBlock() {
        return startAnchor.map(ConnectionAnchor::getBlock);
    }

    /** @return this connection's end anchor, if any. */
    public final Optional<InputAnchor> getInputAnchor() {
        return endAnchor;
    }

    /** @return the downstream block, if any. */
    public final Optional<Block> getInputBlock() {
        return endAnchor.map(ConnectionAnchor::getBlock);
    }

    @Override
    public final void changed(ObservableValue<? extends Number> observable,
            Number oldValue, Number newValue) {
        updateStartEndPositions();
    }

    public final void disconnect(ConnectionAnchor anchor) {
        if (startAnchor.isPresent() && startAnchor.get().equals(anchor)) {
            startAnchor.get().disconnectFrom(this);
            startAnchor = Optional.empty();
        }
        if (endAnchor.isPresent() && endAnchor.get().equals(anchor)) {
            endAnchor.get().disconnectFrom(this);
            endAnchor = Optional.empty();
        }
    }

    public final void disconnect(Optional<? extends ConnectionAnchor> anchor){
        disconnect(anchor.orElse(null));
        if (errorLine.isPresent()){
        ((CustomUIPane)this.getParent()).getChildren().remove(errorLine.get());
        }
    }

    public final void disconnect() {
        disconnect(startAnchor);
        disconnect(endAnchor);
    }

    @Override
    public String toString() {
        return "Connection connecting \n(out) " + startAnchor + "   to\n(in)  "
                + endAnchor;
    }

    /**
     * This method evaluates the validity of the created connection.
     * If the connection results in an invalid operation a visual
     * error will be displayed.
     */
    private void checkError() {
        if(startAnchor.isPresent() && endAnchor.isPresent()) {
            //TODO for debugging this line will always error
            this.getStyleClass().add("error");
            Point2D midPoint = getMidPoint();
            errorLine = Optional.of(new Line(midPoint.getX()-10, midPoint.getY()-10, midPoint.getX()+10, midPoint.getY()+10));
            ((CustomUIPane)this.getParent()).getChildren().add(errorLine.get());
        }
    }
    
    /** Updates the position of the error line*/
    private void updateErrorLines() {
        if (errorLine.isPresent()) {
            errorLine.get().setStartX(getMidPoint().getX()-10);
            errorLine.get().setStartY(getMidPoint().getY()-10);
            errorLine.get().setEndX(getMidPoint().getX()+10);
            errorLine.get().setEndY(getMidPoint().getY()+10);
        }
    }
}
