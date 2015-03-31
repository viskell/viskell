package nl.utwente.group10.ui;

import javafx.scene.Node;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.group10.ui.components.DisplayBlock;
import nl.utwente.group10.ui.components.ConnectionLine;
import nl.utwente.group10.ui.components.OutputAnchor;
import nl.utwente.group10.ui.gestures.UIEvent;
import nl.utwente.group10.ui.gestures.GestureCallBack;

import java.util.Optional;

public class CustomUIPane extends TactilePane implements GestureCallBack {
	private Optional<OutputAnchor> anchor;
	
	
	public CustomUIPane() {
		this.anchor = Optional.empty();
	}

	public void setLastOutputAnchor(OutputAnchor anchor) {
		this.anchor = Optional.ofNullable(anchor);
	}

	public Optional<OutputAnchor> getLastOutputAnchor() {
		return this.anchor;
	}

	@Override
	public void handleCustomEvent(UIEvent event) {
	}

	/** Re-evaluate all displays. */
	public void invalidate() {
		for (Node node : getChildren()) {
			if (node instanceof DisplayBlock) {
				((DisplayBlock)node).invalidate();
			}
		}
	}
	
	public ConnectionLine createLine(){
		ConnectionLine line = new ConnectionLine();
		this.getChildren().add(line);
		return line;
	}
}
