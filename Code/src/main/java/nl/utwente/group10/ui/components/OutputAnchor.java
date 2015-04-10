package nl.utwente.group10.ui.components;

import javafx.scene.input.MouseEvent;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.handlers.OutputAnchorHandler;

import java.io.IOException;
import java.util.Optional;

public class OutputAnchor extends ConnectionAnchor{
    public OutputAnchor(Block block, CustomUIPane pane) throws IOException {
        super(block, pane);
        new OutputAnchorHandler(pane.getConnectionCreationManager(),this);
    }

	@Override
	public Optional<Connection> createConnectionWith(ConnectionAnchor other) {
		if(other instanceof InputAnchor){
	    	return Optional.of(createConnectionTo((InputAnchor) other));
		}else{
			return Optional.empty();
		}
	}
	
	public Connection createConnectionTo(InputAnchor other){
		new Connection(this, other);
		getPane().getChildren().add(getConnection().get());
		getPane().invalidate();
		return getConnection().get();
	}
	
	@Override
	public String toString(){
    	return "OutputAnchor belonging to "+getBlock().getName();
    }

	@Override
	public boolean canConnect() {
		//OutputAnchors can have multiple connections;
		return true;
	}

	@Override
	public Optional<Connection> getConnection(){
    	//Does not keep track of its connections.
		return Optional.empty();
    }
	
	@Override
	public void disconnect(Connection connection) {
		// Currently does not keep track of its connections.
	}
}
