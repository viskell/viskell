package nl.utwente.group10.ui.gestures;

import javafx.scene.Node;

public abstract class AbstractGesture {
	protected Node latchTo;

	public AbstractGesture(Node latchTo) {
		this.latchTo = latchTo;
		latch();
	}
	
	protected abstract void latch();
}
