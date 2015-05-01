package nl.utwente.group10.ui.components;

import java.io.IOException;
import java.util.Optional;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.FunctionBlock;
import nl.utwente.group10.ui.components.blocks.OutputBlock;
import nl.utwente.group10.ui.components.blocks.ValueBlock;
import nl.utwente.group10.ui.handlers.AnchorHandler;

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
        new AnchorHandler(pane.getConnectionCreationManager(),this);
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
    public void disconnectFrom(Connection connection) {
        // Currently does not keep track of its connections.
    }

	@Override
	public Type getType() {
		if(getBlock() instanceof OutputBlock){
			return ((OutputBlock)getBlock()).getOutputType();
		}else{
			return null;
		}
	}
}
