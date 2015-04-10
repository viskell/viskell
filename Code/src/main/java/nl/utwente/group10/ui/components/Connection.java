package nl.utwente.group10.ui.components;

import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;

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
	 * @param anchor
	 *            Anchor to add
	 * @return Whether or not the anchor was added.
	 */
	public boolean addAnchor(ConnectionAnchor anchor) {
		boolean added = false;
		if (!startAnchor.isPresent() && anchor instanceof OutputAnchor) {
			setStartAnchor((OutputAnchor) anchor);
			added = true;
		} else if (!endAnchor.isPresent() && anchor instanceof InputAnchor) {
			setEndAnchor((InputAnchor) anchor);
			added = true;
		}

		return added;
	}

	public boolean isConnected() {
		return startAnchor.isPresent() && endAnchor.isPresent();
	}

	/**
	 * Set the startAnchor for this line. After setting the StartPosition will
	 * be updated.
	 * 
	 * @param Anchor
	 *            to start at.
	 */
	public void setStartAnchor(OutputAnchor start) {
		if (startAnchor.isPresent()) {
			startAnchor.get().getBlock().layoutXProperty().removeListener(this);
			startAnchor.get().getBlock().layoutYProperty().removeListener(this);
		}
		startAnchor = Optional.of(start);
		startAnchor.get().setConnection(this);

		startAnchor.get().getBlock().layoutXProperty().addListener(this);
		startAnchor.get().getBlock().layoutYProperty().addListener(this);
		updateStartPosition();
	}

	/**
	 * Set the endAnchor for this line. After setting the EndPosition will be
	 * updated.
	 * 
	 * @param Anchor
	 *            to end at.
	 */
	public void setEndAnchor(InputAnchor end) {
		if (endAnchor.isPresent()) {
			endAnchor.get().getBlock().layoutXProperty().removeListener(this);
			endAnchor.get().getBlock().layoutYProperty().removeListener(this);
		}
		endAnchor = Optional.of(end);
		endAnchor.get().setConnection(this);

		endAnchor.get().getBlock().layoutXProperty().addListener(this);
		endAnchor.get().getBlock().layoutYProperty().addListener(this);
		updateEndPosition();
	}

	/**
	 * Runs both the update Start end End position functions. Use when
	 * refreshing UI representation of the Line.
	 */
	protected void updateStartEndPositions() {
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
	}

	/**
	 * Refresh the End position of this Line using endAnchor as a reference
	 * point.
	 */
	private void updateEndPosition() {
		if (endAnchor.isPresent()) {
			setEndPosition(getScenePoint(endAnchor.get()));
		}
	}

	public Point2D getScenePoint(ConnectionAnchor anchor) {
		double x = anchor.getCenterX();
		double y = anchor.getCenterY();
		return anchor.localToScene(x, y);
	}

	public Optional<OutputAnchor> getOutputAnchor() {
		return startAnchor;
	}

	public Optional<InputAnchor> getInputAnchor() {
		return endAnchor;
	}

	@Override
	public void changed(ObservableValue<? extends Number> observable,
			Number oldValue, Number newValue) {
		updateStartEndPositions();
	}

	public void disconnect(ConnectionAnchor anchor) {
		if (startAnchor.isPresent() && startAnchor.get().equals(anchor)) {
			startAnchor.get().disconnect(this);
			startAnchor = Optional.empty();
		}
		if (endAnchor.isPresent() && endAnchor.get().equals(anchor)) {
			endAnchor.get().disconnect(this);
			endAnchor = Optional.empty();
		}
	}

	public void disconnect() {
		disconnect(startAnchor.orElse(null));
		disconnect(endAnchor.orElse(null));
	}

	@Override
	public String toString() {
		return "Connection connecting \n(out) " + startAnchor + "   to\n(in)  "
				+ endAnchor;
	}
}
