package nl.utwente.group10.ui;

import java.util.ArrayList;
import java.util.Optional;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import nl.utwente.ewi.caes.tactilefx.control.TactilePane;
import nl.utwente.group10.ui.components.Connection;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.DisplayBlock;
import nl.utwente.group10.ui.handlers.ConnectionCreationManager;

/**
 * Extension of TactilePane that keeps state for the user interface.
 */
public class CustomUIPane extends TactilePane {
	private ObjectProperty<Optional<Block>> selectedBlock;
	private ConnectionCreationManager connectionCreationManager;

	/**
	 * Constructs a new instance.
	 */
	public CustomUIPane() {
		this.connectionCreationManager = new ConnectionCreationManager(this);
		this.selectedBlock = new SimpleObjectProperty<>(Optional.empty());
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

	public Optional<Block> getSelectedBlock() {
		return selectedBlock.get();
	}

	public void setSelectedBlock(Block selectedBlock) {
		this.selectedBlock.set(Optional.ofNullable(selectedBlock));
	}

	public ObjectProperty<Optional<Block>> selectedBlockProperty() {
		return selectedBlock;
	}

	/** Remove the given block from this UI pane, including its connections. */
	public void removeBlock(Block block) {
		Optional<Block> target = Optional.of(block);
		ArrayList<Node> toRemove = new ArrayList<>();

		for (Node node : getChildren()) {
			if (node instanceof Connection) {
				Optional<Block> in = ((Connection) node).getInputBlock();
				Optional<Block> out = ((Connection) node).getOutputBlock();

				if (in.equals(target) || out.equals(target)) {
					toRemove.add(node);
				}
			} else if (node.equals(block)) {
				toRemove.add(node);
			}
		}

		this.getChildren().removeAll(toRemove);
	}

	/** Remove the selected block, if any. */
	public void removeSelected() {
		this.getSelectedBlock().map(obj -> {
			this.removeBlock(obj);
			return obj;
		});
	}

	public ConnectionCreationManager getConnectionCreationManager() {
		return connectionCreationManager;
	}
}
