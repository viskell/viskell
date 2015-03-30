package nl.utwente.group10.ui;

import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
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
}
