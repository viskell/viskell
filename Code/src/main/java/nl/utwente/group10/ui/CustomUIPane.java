package nl.utwente.group10.ui;

import javafx.scene.Node;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.group10.ui.components.DisplayBlock;
import nl.utwente.group10.ui.components.OutputAnchor;
import nl.utwente.group10.ui.gestures.UIEvent;
import nl.utwente.group10.ui.gestures.GestureCallBack;

import java.util.Optional;

/**
 * Extension of TactilePane that keeps state for the user interface.
 */
public class CustomUIPane extends TactilePane implements GestureCallBack {
	/** Optional containing the last selected anchor. */
	private Optional<OutputAnchor> anchor;

	/**
	 * Constructs a new instance.
	 */
	public CustomUIPane() {
		this.anchor = Optional.empty();
	}

	/**
	 * Sets the last selected anchor. This method should be called when an anchor is selected.
	 * @param anchor The currently selected anchor. May be {@code null} if no anchor is selected.
	 */
	public final void setLastOutputAnchor(final OutputAnchor anchor) {
		this.anchor = Optional.ofNullable(anchor);
	}

	/**
	 * @return Optional containing the last selected output anchor.
	 */
	public final Optional<OutputAnchor> getLastOutputAnchor() {
		return this.anchor;
	}

	@Override
	public void handleCustomEvent(UIEvent event) {
	}

	/**
	 * Re-evaluate all display blocks.
	 */
	public final void invalidate() {
		for (Node node : getChildren()) {
			if (node instanceof DisplayBlock) {
				((DisplayBlock)node).invalidate();
			}
		}
	}
}
