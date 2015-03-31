package nl.utwente.group10.ui.gestures;

import java.util.Date;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public abstract class AbstractGesture {
	protected Node latchTo;

	public AbstractGesture(Node latchTo) {
		this.latchTo = latchTo;
		latch();
	}
	
	protected abstract void latch();
}
