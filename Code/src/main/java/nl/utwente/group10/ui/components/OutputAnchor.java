package nl.utwente.group10.ui.components;

import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.handlers.OutputAnchorHandler;

import java.io.IOException;
import java.util.Optional;

/**
 * Anchor that specifically functions as an output.
 */
public class OutputAnchor extends ConnectionAnchor {
	/**
	 * @param block The block this Anchor is connected to.
	 * @param pane The parent pane on which this anchor resides.
	 * @throws IOException when the FXML definition of this anchor cannot be loaded.
	 */
	public OutputAnchor(Block block, CustomUIPane pane) throws IOException {
		super(block, pane);
		new OutputAnchorHandler(pane.getConnectionCreationManager(),this);
	}

	public Connection createConnectionTo(InputAnchor other) {
		new Connection(this, other);
		getPane().getChildren().add(getConnection().get());
		getPane().invalidate();
		return getConnection().get();
	}

	@Override
	public String toString() {
		return "OutputAnchor for " + getBlock();
	}

	@Override
	public boolean canConnect() {
		// OutputAnchors can have multiple connections;
		return true;
	}

	@Override
	public Optional<Connection> getConnection() {
		// Does not keep track of its connections.
		return Optional.empty();
	}

	@Override
	public void disconnect(Connection connection) {
		// Currently does not keep track of its connections.
	}
}
